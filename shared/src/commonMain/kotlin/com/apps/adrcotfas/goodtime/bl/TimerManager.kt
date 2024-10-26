package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Manages the timer state and provides methods to start, pause, resume and finish the timer.
 */
class TimerManager(
    private val localDataRepo: LocalDataRepository,
    private val settingsRepo: SettingsRepository,
    private val listeners: List<EventListener>,
    private val timeProvider: TimeProvider,
    private val finishedSessionsHandler: FinishedSessionsHandler,
    private val streakAndLongBreakHandler: StreakAndLongBreakHandler,
    private val breakBudgetHandler: BreakBudgetHandler,
    private val log: Logger,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private var job: Job? = null
    private val _timerData: MutableStateFlow<DomainTimerData> = MutableStateFlow(DomainTimerData())
    private lateinit var settings: AppSettings

    val timerData: StateFlow<DomainTimerData> = _timerData

    init {
        setup()
    }

    fun setup() {
        job = coroutineScope.launch {
            initAndObserveLabelChange()
            initPersistentData()
        }
    }

    fun restart() {
        job?.cancel()
        setup()
    }

    private suspend fun initPersistentData() {
        settingsRepo.settings.map {
            Pair(it.longBreakData, it.breakBudgetData)
        }.first().let {
            log.i { "new persistentData: $it" }
            _timerData.update { data ->
                data.copy(longBreakData = it.first, breakBudgetData = it.second)
            }
        }
    }

    private suspend fun initAndObserveLabelChange() {
        settingsRepo.settings.map {
            settings = it
            it.labelName
        }.distinctUntilChanged().flatMapLatest { labelName ->
            log.i { "new active label: $labelName" }
            _timerData.update { data -> data.copy(labelName = labelName) }
            localDataRepo.selectLabelByName(labelName)
                .combine(localDataRepo.selectDefaultLabel()) { label, defaultLabel ->
                    if (label == null) {
                        settingsRepo.activateDefaultLabel()
                        defaultLabel!!.timerProfile
                    } else {
                        if (label.useDefaultTimeProfile) defaultLabel!!.timerProfile else label.timerProfile
                    }
                }
        }.distinctUntilChanged()
            .collect {
                log.i { "new timerProfile: $it" }
                _timerData.update { data -> data.copy(timerProfile = it) }
            }
    }

    fun start(timerType: TimerType = timerData.value.type) {
        val data = timerData.value
        if (data.timerProfile == null) {
            log.e { "Trying to start the timer without a label" }
            return
        }

        val isPaused = data.state.isPaused
        val elapsedRealTime = timeProvider.elapsedRealtime()

        val newTimerData = timerData.value.copy(
            startTime = if (isPaused) {
                data.startTime
            } else {
                elapsedRealTime
            },
            lastStartTime = elapsedRealTime,
            endTime = if (isPaused) {
                elapsedRealTime + data.remainingTimeAtPause
            } else {
                data.requireTimerProfile().endTime(
                    timerType,
                    elapsedRealTime
                )
            },
            state = TimerState.RUNNING,
            type = if (isPaused) {
                data.type
            } else {
                timerType
            },
            pausedTime = if (isPaused) {
                data.pausedTime
            } else {
                0
            },
        )

        _timerData.update { newTimerData }

        handlePersistentDataAtStart()

        log.i { "Starting $timerType timer: $data" }
        val autoStarted = (timerType.isWork && settings.autoStartWork)
                || (timerType.isBreak && settings.autoStartBreak)

        listeners.forEach {
            it.onEvent(
                Event.Start(
                    autoStarted = autoStarted,
                    endTime = newTimerData.endTime
                )
            )
        }
    }

    fun addOneMinute() {
        val data = timerData.value
        if (!data.state.isActive) {
            log.e { "Trying to add one minute when the timer is not running" }
            return
        }
        if (!data.requireTimerProfile().isCountdown) {
            log.e { "Trying to add a minute to a timer that is not a countdown" }
            return
        }
        val newEndTime = data.endTime + 1.minutes.inWholeMilliseconds
        val newRemainingTimeAtPause = if (data.state.isPaused) {
            data.remainingTimeAtPause + 1.minutes.inWholeMilliseconds
        } else 0

        _timerData.update {
            it.copy(
                endTime = newEndTime,
                remainingTimeAtPause = newRemainingTimeAtPause
            )
        }
        log.i { "Added one minute" }
        listeners.forEach { it.onEvent(Event.AddOneMinute(newEndTime)) }
    }

    fun toggle() {
        when (timerData.value.state) {
            TimerState.RUNNING -> pause()
            TimerState.PAUSED -> resume()
            else -> log.e { "Trying to toggle the timer when it is not running or paused" }
        }
    }

    private fun pause() {
        val elapsedRealtime = timeProvider.elapsedRealtime()
        _timerData.update {
            it.copy(
                remainingTimeAtPause = it.endTime - elapsedRealtime,
                lastPauseTime = elapsedRealtime,
                state = TimerState.PAUSED
            )
        }
        log.i { "Paused: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Pause) }
    }

    private fun resume() {
        val elapsedRealTime = timeProvider.elapsedRealtime()
        updatePausedTime()
        _timerData.update {
            it.copy(
                lastStartTime = elapsedRealTime,
                endTime = it.remainingTimeAtPause + elapsedRealTime,
                state = TimerState.RUNNING,
                remainingTimeAtPause = 0
            )
        }
        log.i { "Resumed: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Start(endTime = timerData.value.endTime)) }
    }

    private fun updatePausedTime() {
        val data = timerData.value
        if (data.lastPauseTime != 0L) {
            val elapsedRealTime = timeProvider.elapsedRealtime()
            val pausedTime = data.pausedTime + elapsedRealTime - data.lastPauseTime
            log.i { "Paused time: ${pausedTime.milliseconds}" }
            _timerData.update {
                it.copy(pausedTime = pausedTime, lastPauseTime = 0)
            }
        }
    }

    /**
     * Skips the current session and starts the next one.
     * This is called manually by the user before a session is finished, interrupting the current session.
     */
    fun skip() {
        nextInternal(updateWorkTime = false, finishActionType = FinishActionType.MANUAL_SKIP)
    }

    fun next(
        updateWorkTime: Boolean = false,
        finishActionType: FinishActionType = FinishActionType.MANUAL_NEXT
    ) {
        nextInternal(updateWorkTime, finishActionType)
    }

    /**
     * Called automatically when autoStart is enabled and the time is up or manually at the end of a session.
     */
    private fun nextInternal(updateWorkTime: Boolean = false, finishActionType: FinishActionType) {
        val data = timerData.value
        val state = data.state
        val timerProfile = data.timerProfile

        if (state == TimerState.RESET) {
            log.e { "Trying to start the next session but the timer is reset" }
            return
        }
        if (timerProfile == null) {
            log.e { "Trying to start the next session without a profile" }
            return
        }

        val isWork = data.type.isWork
        val isCountDown = data.requireTimerProfile().isCountdown

        handleFinishedSession(updateWorkTime, finishActionType = finishActionType)

        _timerData.update { it.reset() }

        val nextType = when {
            !isWork || (isWork && !timerProfile.isBreakEnabled) -> TimerType.WORK
            !isCountDown -> TimerType.BREAK
            shouldConsiderStreak(timeProvider.elapsedRealtime()) -> TimerType.LONG_BREAK
            else -> TimerType.BREAK
        }
        log.i { "Next: $nextType" }
        start(nextType)
    }

    /**
     * Called when the time is up for countdown timers.
     * A finished [Session] is created and sent to the listeners.
     */
    fun finish() {
        val data = timerData.value
        val state = data.state
        val timerProfile = data.timerProfile
        val type = data.type

        if (state.isReset || state.isFinished) {
            log.e { "Trying to finish the timer when it is reset or finished" }
            return
        }
        if (timerProfile == null) {
            log.e { "Trying to finish the timer without a profile" }
            return
        }

        _timerData.update { it.copy(state = TimerState.FINISHED) }
        log.i { "Finish: $data" }

        handleFinishedSession(finishActionType = FinishActionType.AUTO)

        val autoStart =
            settings.autoStartWork && (type.isBreak || !timerProfile.isBreakEnabled)
                    || settings.autoStartBreak && type.isWork && timerProfile.isBreakEnabled
        log.i { "AutoStart: $autoStart" }
        listeners.forEach {
            it.onEvent(
                Event.Finished(
                    type = type,
                    autostartNextSession = autoStart
                )
            )
        }
        if (autoStart) {
            next(finishActionType = FinishActionType.AUTO)
        }
    }

    /**
     * Resets(stops) the timer.
     * This is also part of the flow after [finish] when the user has the option of starting a new session.
     * @param updateWorkTime if true, the duration of the already saved session will be updated.
     *                       This is useful when the user missed the notification and continued working.
     * @see [finish]
     */
//TODO: implement logic for updateWorkTime
    fun reset(updateWorkTime: Boolean = false) {
        val data = timerData.value
        if (data.state == TimerState.RESET) {
            log.w { "Trying to reset the timer when it is already reset" }
            return
        }
        log.i { "Reset: $data" }

        handleFinishedSession(updateWorkTime, finishActionType = FinishActionType.MANUAL_RESET)

        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.update { it.reset() }
    }

    private fun handlePersistentDataAtStart() {
        if (timerData.value.type == TimerType.WORK) {
            // filter out the case when some time passes since the last work session
            // preemptively reset the streak if the current work session cannot end in time
            resetStreakIfNeeded(timerData.value.endTime)

            // update the break budget if the timer is count-up
            if (!timerData.value.requireTimerProfile().isCountdown) {
                val existingBudget =
                    timerData.value.breakBudgetData.getRemainingBreakBudget(timeProvider.elapsedRealtime())
                val breakBudgetData = BreakBudgetData(existingBudget, 0)
                _timerData.update { it.copy(breakBudgetData = breakBudgetData) }
                breakBudgetHandler.updateBreakBudget(breakBudgetData)
            }
        }
    }

    private fun handleFinishedSession(
        updateWorkTime: Boolean = false,
        finishActionType: FinishActionType
    ) {
        val data = timerData.value
        val isWork = data.type.isWork
        val isFinished = data.state.isFinished
        val isCountDown = data.requireTimerProfile().isCountdown

        val session = createFinishedSession()
        session?.let {
            if (isFinished && updateWorkTime) {
                finishedSessionsHandler.updateSession(it)
                return
            } else if (!isFinished || (isFinished
                        && (finishActionType != FinishActionType.MANUAL_NEXT))
            ) {
                finishedSessionsHandler.saveSession(it)
            }

            if (isWork && !isCountDown) {
                val millis = timeProvider.elapsedRealtime()
                if (data.breakBudgetData.breakBudgetStart == 0L) {
                    val existingBudget = data.breakBudgetData.breakBudget
                    val breakBudgetData = BreakBudgetData(
                        data.getBreakBudget(session.duration.toInt()) + existingBudget,
                        millis
                    )
                    log.i { "Updating breakBudget: $breakBudgetData" }
                    _timerData.update { data -> data.copy(breakBudgetData = breakBudgetData) }
                    breakBudgetHandler.updateBreakBudget(breakBudgetData)
                } else {
                    log.e { "The breakBudgetStart should be 0 at this point" }
                }
            }
        }

        if (isWork && isCountDown
            && (finishActionType == FinishActionType.AUTO
                    || finishActionType == FinishActionType.MANUAL_SKIP)
        ) {
            incrementStreak()
        }
    }

    private fun createFinishedSession(): Session? {
        updatePausedTime()
        val data = timerData.value
        val isWork = data.type == TimerType.WORK

        val totalDuration = timeProvider.elapsedRealtime() - data.startTime

        val durationToSave = if (isWork) {
            val justWorkTime =
                (totalDuration - data.pausedTime + WIGGLE_ROOM_MILLIS).milliseconds
            justWorkTime
        } else {
            totalDuration.milliseconds
        }

        val durationToSaveMinutes = durationToSave.inWholeMinutes
        if (durationToSaveMinutes < 1) {
            log.i { "The session was shorter than 1 minute: $durationToSave" }
            return null
        }

        val startTimeMillis = data.startTime
        val endTimeInMillis = timeProvider.elapsedRealtime()

        return Session.create(
            startTimeMillis,
            endTimeInMillis,
            durationToSaveMinutes,
            data.requireLabelName(),
            isWork
        )
    }

    private fun incrementStreak() {
        val lastWorkEndTime = timeProvider.elapsedRealtime()
        _timerData.update {
            it.copy(
                longBreakData = it.longBreakData.copy(
                    streak = it.longBreakData.streak + 1,
                    lastWorkEndTime = lastWorkEndTime
                )
            )
        }
        streakAndLongBreakHandler.incrementStreak(lastWorkEndTime)
    }

    //TODO: call this from the view when visible to make sure we have the latest data
    fun resetStreakIfNeeded(millis: Long = timeProvider.elapsedRealtime()) {
        if (!didLastWorkSessionFinishRecently(millis)) {
            _timerData.update {
                it.copy(
                    longBreakData = it.longBreakData.copy(
                        streak = 0,
                        lastWorkEndTime = 0L
                    )
                )
            }
            streakAndLongBreakHandler.resetStreak()
        }
    }

    private fun shouldConsiderStreak(workEndTime: Long): Boolean {
        val data = timerData.value
        val timerProfile = data.timerProfile
        if (timerProfile?.isCountdown != true) return false

        val streakForLongBreakIsReached =
            (data.longBreakData.streak % timerProfile.sessionsBeforeLongBreak == 0)
        return streakForLongBreakIsReached && didLastWorkSessionFinishRecently(
            workEndTime
        )
    }

    private fun didLastWorkSessionFinishRecently(workEndTime: Long): Boolean {
        val data = timerData.value
        val timerProfile = data.timerProfile
        if (timerProfile?.isCountdown != true) return false

        val maxIdleTime = timerProfile.workDuration.minutes.inWholeMilliseconds +
                timerProfile.breakDuration.minutes.inWholeMilliseconds +
                30.minutes.inWholeMilliseconds
        return data.longBreakData.lastWorkEndTime != 0L && max(
            0,
            workEndTime - data.longBreakData.lastWorkEndTime
        ) < maxIdleTime
    }

    companion object {
        const val WIGGLE_ROOM_MILLIS = 100
    }
}

enum class FinishActionType {
    MANUAL_RESET,
    MANUAL_SKIP,
    MANUAL_NEXT,
    AUTO
}