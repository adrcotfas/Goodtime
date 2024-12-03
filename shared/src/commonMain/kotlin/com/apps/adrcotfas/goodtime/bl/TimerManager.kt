package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.streakInUse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import kotlin.time.Duration
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
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    private var mainJob: Job? = null

    private val _timerData: MutableStateFlow<DomainTimerData> = MutableStateFlow(DomainTimerData())
    private lateinit var settings: AppSettings

    val timerData: StateFlow<DomainTimerData> = _timerData

    init {
        setup()
    }

    fun setup() {
        mainJob = coroutineScope.launch {
            initAndObserveLabelChange()
        }
        initAndObservePersistentData()
    }

    fun restart() {
        mainJob?.cancel()
        setup()
    }

    private suspend fun initAndObserveLabelChange() {
        settingsRepo.settings.map {
            settings = it
            it.labelName
        }.distinctUntilChanged().flatMapLatest { labelName ->
            localDataRepo.selectLabelByName(labelName)
                .combine(localDataRepo.selectDefaultLabel()) { label, defaultLabel ->
                    val defaultTimerProfile = defaultLabel!!.timerProfile
                    if (label == null) {
                        settingsRepo.activateDefaultLabel()
                        DomainLabel(defaultLabel, defaultTimerProfile)
                    } else {
                        DomainLabel(
                            label,
                            if (label.useDefaultTimeProfile) defaultTimerProfile else label.timerProfile
                        )
                    }
                }
        }.distinctUntilChanged()
            .collect {
                log.i { "new timerProfile: $it" }
                _timerData.update { data ->
                    data.copy(
                        isReady = true,
                        label = it
                    )
                }
            }
    }

    private fun initAndObservePersistentData() {
        coroutineScope.launch {
            settingsRepo.settings.map { it.longBreakData }
                .distinctUntilChanged()
                .collect {
                    log.i { "new long break data: $it" }
                    _timerData.update { data -> data.copy(longBreakData = it) }
                }
        }
        coroutineScope.launch {
            settingsRepo.settings.map { it.breakBudgetData }
                .first().let {
                    log.i { "new break budget: ${it.getRemainingBreakBudget(timeProvider.elapsedRealtime())}" }
                    _timerData.update { data -> data.copy(breakBudgetData = it) }
                }
        }
    }

    fun start(timerType: TimerType = timerData.value.type, autoStarted: Boolean = false) {
        log.i { "Starting timer..." }
        val data = timerData.value
        if (!data.isReady) {
            log.e { "timer data not ready" }
            return
        }

        val isPaused = data.state.isPaused
        val elapsedRealTime = timeProvider.elapsedRealtime()

        if (data.state.isReset) {
            updateBreakBudgetIfNeeded()
        }

        val newTimerData = timerData.value.copy(
            startTime = if (isPaused) {
                data.startTime
            } else {
                elapsedRealTime
            },
            lastStartTime = elapsedRealTime,
            endTime = if (isPaused) {
                //TODO: check correctness of timeAtPause for count-up sessions
                elapsedRealTime + data.timeAtPause
            } else {
                data.getEndTime(timerType, elapsedRealTime)
            },
            state = TimerState.RUNNING,
            type = if (isPaused) {
                data.type
            } else {
                timerType
            },
            timeSpentPaused = if (isPaused) {
                data.timeSpentPaused
            } else {
                0
            },
        )

        _timerData.update { newTimerData }

        handlePersistentDataAtStart()

        listeners.forEach {
            it.onEvent(
                Event.Start(
                    autoStarted = autoStarted,
                    endTime = newTimerData.endTime
                )
            )
        }
    }

    private fun updateBreakBudgetIfNeeded(): Duration {
        if (!timerData.value.label.isCountdown) {
            val elapsedRealtime = timeProvider.elapsedRealtime()
            val breakBudget = timerData.value.getBreakBudget(elapsedRealtime)
            log.v { "Persisting break budget: $breakBudget" }
            _timerData.update {
                it.copy(
                    breakBudgetData = BreakBudgetData(
                        breakBudget = breakBudget,
                        breakBudgetStart = elapsedRealtime
                    )
                )
            }

            breakBudgetHandler.updateBreakBudget(
                BreakBudgetData(
                    breakBudget = breakBudget,
                    breakBudgetStart = elapsedRealtime
                )
            )
            return breakBudget
        }
        return 0.minutes
    }

    fun addOneMinute() {
        val data = timerData.value
        if (!data.state.isActive) {
            log.e { "Trying to add one minute when the timer is not running" }
            return
        }
        if (!data.getTimerProfile().isCountdown) {
            log.e { "Trying to add a minute to a timer that is not a countdown" }
            return
        }
        val newEndTime = data.endTime + 1.minutes.inWholeMilliseconds
        val newRemainingTimeAtPause = if (data.state.isPaused) {
            data.timeAtPause + 1.minutes.inWholeMilliseconds
        } else 0

        _timerData.update {
            it.copy(
                endTime = newEndTime,
                timeAtPause = newRemainingTimeAtPause
            )
        }
        log.i { "Added one minute" }
        listeners.forEach { it.onEvent(Event.AddOneMinute(newEndTime)) }
    }

    fun canToggle() = !timerData.value.type.isBreak

    fun toggle() {
        when (timerData.value.state) {
            TimerState.RUNNING -> pause()
            TimerState.PAUSED -> resume()
            else -> log.e { "Trying to toggle the timer when it is not running or paused" }
        }
    }

    private fun pause() {
        val elapsedRealtime = timeProvider.elapsedRealtime()
        updateBreakBudgetIfNeeded()
        _timerData.update {
            it.copy(
                timeAtPause =
                if (it.label.profile.isCountdown) {
                    it.endTime - elapsedRealtime
                } else {
                    elapsedRealtime - it.startTime - it.timeSpentPaused
                },
                lastPauseTime = elapsedRealtime,
                state = TimerState.PAUSED
            )
        }
        log.i { "Paused: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Pause) }
    }

    private fun resume() {
        val elapsedRealTime = timeProvider.elapsedRealtime()
        updateBreakBudgetIfNeeded()
        updatePausedTime()
        val isCountdown = timerData.value.label.profile.isCountdown
        _timerData.update {
            it.copy(
                lastStartTime = elapsedRealTime,
                endTime = if (isCountdown) {
                    it.timeAtPause + elapsedRealTime
                } else {
                    it.endTime
                },
                state = TimerState.RUNNING,
                timeAtPause = 0
            )
        }
        log.i { "Resumed: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Start(endTime = timerData.value.endTime)) }
    }

    private fun updatePausedTime() {
        val data = timerData.value
        if (data.lastPauseTime != 0L) {
            val elapsedRealTime = timeProvider.elapsedRealtime()
            val pausedTime = data.timeSpentPaused + elapsedRealTime - data.lastPauseTime
            log.i { "updatePausedTime: ${pausedTime.milliseconds}" }
            _timerData.update {
                it.copy(timeSpentPaused = pausedTime, lastPauseTime = 0)
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
        if (!data.isReady) {
            log.e { "timer data not ready" }
            return
        }
        val state = data.state
        val timerProfile = data.label

        if (state == TimerState.RESET) {
            log.e { "Trying to start the next session but the timer is reset" }
            return
        }

        val isWork = data.type.isWork
        val isCountDown = data.getTimerProfile().isCountdown

        updateBreakBudgetIfNeeded()

        val breakBudget = data.getBreakBudget(timeProvider.elapsedRealtime())
        if (isWork && !isCountDown && breakBudget < 1.minutes) {
            log.e { "Break budget is depleted, cannot start break" }
            return
        }

        handleFinishedSession(updateWorkTime, finishActionType = finishActionType)

        _timerData.update { it.reset() }

        val nextType = when {
            !isWork || (isWork && !timerProfile.profile.isBreakEnabled) -> TimerType.WORK
            !isCountDown -> TimerType.BREAK
            shouldConsiderStreak(timeProvider.elapsedRealtime()) -> TimerType.LONG_BREAK
            else -> TimerType.BREAK
        }
        log.i { "Next: $nextType" }

        val autoStarted = (nextType.isWork && settings.autoStartWork)
                || (nextType.isBreak && settings.autoStartBreak)

        start(nextType, autoStarted)
    }

    /**
     * Called when the time is up for countdown timers.
     * A finished [Session] is created and sent to the listeners.
     */
    fun finish() {
        val data = timerData.value
        if (!data.isReady) {
            log.e { "timer data not ready" }
            return
        }

        val state = data.state
        val timerProfile = data.label
        val type = data.type

        if (state.isReset || state.isFinished) {
            log.e { "Trying to finish the timer when it is reset or finished" }
            return
        }

        _timerData.update { it.copy(state = TimerState.FINISHED) }
        log.i { "Finish: $data" }

        updateBreakBudgetIfNeeded()
        handleFinishedSession(finishActionType = FinishActionType.AUTO)

        val autoStart =
            settings.autoStartWork && (type.isBreak || !timerProfile.profile.isBreakEnabled)
                    || settings.autoStartBreak && type.isWork && timerProfile.profile.isBreakEnabled
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
        updateBreakBudgetIfNeeded()

        handleFinishedSession(updateWorkTime, finishActionType = FinishActionType.MANUAL_RESET)

        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.update { it.reset() }
    }

    //TODO: test with a long paused work session too
    private fun handlePersistentDataAtStart() {
        if (timerData.value.type == TimerType.WORK) {
            // filter out the case when some time passes since the last work session
            // preemptively reset the streak if the current work session cannot end in time
            resetStreakIfNeeded(timerData.value.endTime)
        }
    }

    private fun handleFinishedSession(
        updateWorkTime: Boolean = false,
        finishActionType: FinishActionType
    ) {
        val data = timerData.value
        val isWork = data.type.isWork
        val isFinished = data.state.isFinished
        val isCountDown = data.getTimerProfile().isCountdown

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
                (totalDuration - data.timeSpentPaused + WIGGLE_ROOM_MILLIS).milliseconds
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
            data.getLabelName(),
            isWork
        )
    }

    private fun incrementStreak() {
        val lastWorkEndTime = timeProvider.elapsedRealtime()
        streakAndLongBreakHandler.incrementStreak(lastWorkEndTime)
    }

    fun resetStreakIfNeeded(millis: Long = timeProvider.elapsedRealtime()) {
        if (!didLastWorkSessionFinishRecently(millis)) {
            streakAndLongBreakHandler.resetStreak()
        }
    }

    private fun shouldConsiderStreak(workEndTime: Long): Boolean {
        val data = timerData.value
        val timerProfile = data.label
        if (!timerProfile.profile.isCountdown) return false

        val streakForLongBreakIsReached =
            (data.longBreakData.streakInUse(timerProfile.profile.sessionsBeforeLongBreak) == 0)
        return streakForLongBreakIsReached && didLastWorkSessionFinishRecently(
            workEndTime
        )
    }

    private fun didLastWorkSessionFinishRecently(workEndTime: Long): Boolean {
        val data = timerData.value
        val timerProfile = data.label
        if (!timerProfile.profile.isCountdown) return false

        val maxIdleTime = timerProfile.profile.workDuration.minutes.inWholeMilliseconds +
                timerProfile.profile.longBreakDuration.minutes.inWholeMilliseconds +
                15.minutes.inWholeMilliseconds
        return data.longBreakData.lastWorkEndTime != 0L && max(
            0,
            workEndTime - data.longBreakData.lastWorkEndTime
        ) < maxIdleTime
    }

    companion object {
        const val WIGGLE_ROOM_MILLIS = 900L
    }
}

enum class FinishActionType {
    MANUAL_RESET,
    MANUAL_SKIP, // increment streak even if session is shorter than 1 minute
    MANUAL_NEXT,
    AUTO
}