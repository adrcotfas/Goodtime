package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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
    private val log: Logger
) {

    private val _timerData: MutableStateFlow<DomainTimerData> = MutableStateFlow(DomainTimerData())
    private lateinit var settings: AppSettings

    val timerData: StateFlow<DomainTimerData> = _timerData

    suspend fun init() {
        initPersistentData()
        initAndObserveLabelChange()
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
        }.distinctUntilChanged().flatMapLatest {
            log.i { "new label from settingsRepo: $it" }
            it?.let {
                localDataRepo.selectLabelByName(it)
            } ?: localDataRepo.selectDefaultLabel()
        }.distinctUntilChanged().collect {
            _timerData.update { data -> data.copy(label = it) }
            log.i { "new label: ${it.name}" }
        }
    }

    fun start(timerType: TimerType = timerData.value.type) {
        if (timerData.value.label == null) {
            log.e { "Trying to start the timer without a label" }
            return
        }

        val elapsedRealTime = timeProvider.elapsedRealtime()
        if (timerData.value.state == TimerState.PAUSED) {
            _timerData.update {
                it.copy(
                    lastStartTime = elapsedRealTime,
                    endTime = elapsedRealTime + it.remainingTimeAtPause,
                    state = TimerState.RUNNING
                )
            }
        } else {
            _timerData.update {
                it.copy(
                    startTime = elapsedRealTime,
                    lastStartTime = elapsedRealTime,
                    endTime = it.label!!.timerProfile.endTime(
                        timerType,
                        elapsedRealTime
                    ),
                    type = timerType,
                    state = TimerState.RUNNING,
                    pausedTime = 0
                )
            }
        }

        handlePersistentDataAtStart()

        log.i { "Starting $timerType timer: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Start(timerData.value.endTime)) }
    }

    fun addOneMinute() {
        val data = timerData.value
        if (data.state != TimerState.RUNNING) {
            log.e { "Trying to add one minute when the timer is not running" }
            return
        }
        if (!data.label!!.timerProfile.isCountdown) {
            log.e { "Trying to add a minute to a timer that is not a countdown" }
            return
        }

        _timerData.update {
            it.copy(
                endTime = it.endTime + 1.minutes.inWholeMilliseconds,
            )
        }
        log.i { "Added one minute" }
        listeners.forEach { it.onEvent(Event.AddOneMinute(timerData.value.endTime)) }
    }

    fun toggle() {
        when (timerData.value.state) {
            TimerState.RUNNING -> pause()
            TimerState.PAUSED -> resume()
            else -> log.e { "Trying to toggle the timer when it is not running or paused" }
        }
    }

    private fun pause() {
        _timerData.update {
            it.copy(
                remainingTimeAtPause = it.endTime - timeProvider.elapsedRealtime(),
                state = TimerState.PAUSED
            )
        }
        log.i { "Paused: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Pause) }
    }

    private fun resume() {
        val data = timerData.value
        val elapsedRealTime = timeProvider.elapsedRealtime()
        val durationToFinish = data.getDuration()
        val pausedTime =
            data.pausedTime + elapsedRealTime - (durationToFinish - data.remainingTimeAtPause)
        _timerData.update {
            it.copy(
                lastStartTime = elapsedRealTime,
                endTime = it.remainingTimeAtPause + elapsedRealTime,
                state = TimerState.RUNNING,
                remainingTimeAtPause = 0,
                pausedTime = pausedTime
            )
        }
        log.i { "Resumed: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Start(timerData.value.endTime)) }
    }

    fun next(updateWorkTime: Boolean = false) {
        val data = timerData.value
        val state = data.state
        if (state == TimerState.RESET) {
            log.e { "Trying to start the next session but the timer is reset" }
            return
        }

        val isWork = data.type == TimerType.WORK
        val isCountDown = data.label!!.timerProfile.isCountdown

        handleFinishedSession(updateWorkTime, isManualAction = true)

        _timerData.update { it.reset() }

        val nextType = when {
            !isWork -> TimerType.WORK
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
        if (data.state == TimerState.RESET || data.state == TimerState.FINISHED) {
            log.e { "Trying to finish the timer when it is reset or finished" }
            return
        }
        _timerData.update { it.copy(state = TimerState.FINISHED) }
        log.i { "Finish: $data" }

        handleFinishedSession(isManualAction = false)

        val autoStart = settings.autoStartWork && data.type != TimerType.WORK
                || settings.autoStartBreak && data.type == TimerType.WORK
        log.i { "AutoStart: $autoStart" }
        listeners.forEach { it.onEvent(Event.Finished(autoStart)) }
        if (autoStart) {
            next()
        }
    }

    /**
     * Resets(stops) the timer.
     * This is also part of the flow after [finish] when the user has the option of starting a new session.
     * @param updateWorkTime if true, the duration of the already saved session will be updated.
     *                       This is useful when the user missed the notification and continued working.
     * @see [finish]
     */
    fun reset(updateWorkTime: Boolean = false) {
        val data = timerData.value
        if (data.state == TimerState.RESET) {
            log.w { "Trying to reset the timer when it is already reset" }
            return
        }
        log.i { "Reset: $data" }

        handleFinishedSession(updateWorkTime, isManualAction = true)

        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.update { it.reset() }
    }

    private fun handlePersistentDataAtStart() {
        if (timerData.value.type == TimerType.WORK) {
            // filter out the case when some time passes since the last work session
            // preemptively reset the streak if the current work session cannot end in time
            resetStreakIfNeeded(timerData.value.endTime)

            // update the break budget if the timer is count-up
            if (!timerData.value.label!!.timerProfile.isCountdown) {
                val existingBudget =
                    timerData.value.breakBudgetData.getRemainingBreakBudget(timeProvider.elapsedRealtime())
                val breakBudgetData = BreakBudgetData(existingBudget, 0)
                _timerData.update { it.copy(breakBudgetData = breakBudgetData) }
                breakBudgetHandler.updateBreakBudget(breakBudgetData)
            }
        }
    }

    private fun handleFinishedSession(updateWorkTime: Boolean = false, isManualAction: Boolean) {
        val data = timerData.value
        val state = data.state
        val isWork = data.type == TimerType.WORK
        val isFinished = state == TimerState.FINISHED
        val isCountDown = data.label!!.timerProfile.isCountdown

        val session = createFinishedSession()
        session?.let {
            if (isFinished && updateWorkTime) {
                finishedSessionsHandler.updateSession(it)
            } else if (!isFinished || (isFinished && !isManualAction)) {
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

        if (isWork && ((isFinished && !isManualAction) || (!isFinished && isCountDown))) {
            incrementStreak()
        }
    }

    private fun createFinishedSession(): Session? {
        val data = timerData.value
        val isWork = data.type == TimerType.WORK

        val totalDuration = timeProvider.elapsedRealtime() - data.startTime

        val durationToSave = if (isWork) {
            val justWorkTime =
                (totalDuration - data.pausedTime + WIGGLE_ROOM_MILLIS).milliseconds.inWholeMinutes
            justWorkTime
        } else {
            totalDuration.milliseconds.inWholeMinutes
        }

        if (durationToSave < 1) {
            log.i { "The session was shorter than 1 minute: $durationToSave millis" }
            return null
        }

        val startTimeMillis = timeProvider.now() - totalDuration
        val endTimeInMillis = timeProvider.now()

        return Session.create(
            startTimeMillis,
            endTimeInMillis,
            durationToSave,
            data.label!!.name,
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

    private fun shouldConsiderStreak(nextWorkEndTime: Long): Boolean {
        val data = timerData.value
        val label = data.label
        if (label?.timerProfile?.isCountdown != true) return false

        val streakForLongBreakIsReached =
            (data.longBreakData.streak % label.timerProfile.sessionsBeforeLongBreak == 0)
        return streakForLongBreakIsReached && didLastWorkSessionFinishRecently(
            nextWorkEndTime
        )
    }

    private fun didLastWorkSessionFinishRecently(workEndTime: Long): Boolean {
        val data = timerData.value
        val label = data.label
        if (label?.timerProfile?.isCountdown != true) return false

        val maxIdleTime = label.timerProfile.workDuration.minutes.inWholeMilliseconds +
                label.timerProfile.breakDuration.minutes.inWholeMilliseconds +
                30.minutes.inWholeMilliseconds
        return data.longBreakData.lastWorkEndTime != 0L && max(
            0,
            workEndTime - data.longBreakData.lastWorkEndTime
        ) < maxIdleTime
    }

    companion object {
        const val WIGGLE_ROOM_MILLIS = 1000
    }
}