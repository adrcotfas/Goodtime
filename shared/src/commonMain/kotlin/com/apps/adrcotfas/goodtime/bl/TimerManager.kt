package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    private val log: Logger
) {

    private val _timerData: MutableStateFlow<DomainTimerData> = MutableStateFlow(DomainTimerData())
    val timerData: StateFlow<DomainTimerData> = _timerData

    suspend fun init() {
        initPersistentData()
        initAndObserveLabelChange()
    }

    private suspend fun initPersistentData() {
        settingsRepo.settings.map {
            Pair(it.longBreakData, it.breakBudgetData)
        }.first().let {
            log.v { "new persistentData: $it" }
            _timerData.value =
                timerData.value.copy(longBreakData = it.first, breakBudgetData = it.second)
        }
    }

    private suspend fun initAndObserveLabelChange() {
        settingsRepo.settings.map {
            it.labelName
        }.flatMapLatest {
            log.v { "new label from settingsRepo: $it" }
            it?.let {
                localDataRepo.selectLabelByName(it)
            } ?: localDataRepo.selectDefaultLabel()
        }.distinctUntilChanged().collect {
            _timerData.value = _timerData.value.copy(label = it)
            log.v { "new label: ${it.name}" }
        }
    }

    fun start(timerType: TimerType = _timerData.value.type) {
        if (_timerData.value.label == null) {
            log.e { "Trying to start the timer without a label" }
            return
        }

        val elapsedRealTime = timeProvider.elapsedRealtime()
        if (_timerData.value.state == TimerState.PAUSED) {
            _timerData.value = _timerData.value.copy(
                lastStartTime = elapsedRealTime,
                endTime = elapsedRealTime + _timerData.value.remainingTimeAtPause,
                state = TimerState.RUNNING
            )
        } else {
            _timerData.value = _timerData.value.copy(
                startTime = elapsedRealTime,
                lastStartTime = elapsedRealTime,
                endTime = _timerData.value.label!!.timerProfile.endTime(
                    timerType,
                    elapsedRealTime
                ),
                type = timerType,
                state = TimerState.RUNNING,
                pausedTime = 0
            )
        }

        // filter out the case when some time passes since the last work session
        // preemptively reset the streak if the current work session cannot end in time
        if (timerData.value.type == TimerType.WORK) {
            resetStreakIfNeeded(timerData.value.endTime)
        }

        log.v { "Starting timer: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Start(timerData.value.endTime)) }
        //TODO:
        // -toggle fullscreen
        // -toggle dnd mode
    }

    fun addOneMinute() {
        if (_timerData.value.state != TimerState.RUNNING) {
            log.e { "Trying to add one minute when the timer is not running" }
            return
        }
        val data = _timerData.value
        _timerData.value = data.copy(
            endTime = data.endTime + 1.minutes.inWholeMilliseconds,
        )
        log.v { "Added one minute" }
        listeners.forEach { it.onEvent(Event.AddOneMinute(timerData.value.endTime)) }
    }

    fun pause() {
        if (_timerData.value.state != TimerState.RUNNING) {
            log.e { "Trying to pause the timer when it is not running" }
            return
        }
        val data = _timerData.value
        _timerData.value = data.copy(
            remainingTimeAtPause = data.endTime - timeProvider.elapsedRealtime(),
            state = TimerState.PAUSED
        )
        log.v { "Paused: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Pause) }
    }

    fun resume() {
        if (_timerData.value.state != TimerState.PAUSED) {
            log.e { "Trying to resume the timer when it is not paused" }
            return
        }
        val data = _timerData.value
        val elapsedRealTime = timeProvider.elapsedRealtime()
        val durationToFinish = data.getDuration()
        val pausedTime =
            data.pausedTime + elapsedRealTime - (durationToFinish - data.remainingTimeAtPause)
        _timerData.value = data.copy(
            lastStartTime = elapsedRealTime,
            endTime = data.remainingTimeAtPause + elapsedRealTime,
            state = TimerState.RUNNING,
            remainingTimeAtPause = 0,
            pausedTime = pausedTime
        )
        log.v { "Resumed: ${timerData.value}" }
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
        val isFinished = state == TimerState.FINISHED
        val skippedSession = if (isFinished) {
            null
        } else createFinishedSession()
        val updatedSession = if (isFinished && updateWorkTime) {
            createFinishedSession()
        } else null

        // Transition from the finished state to the next session
        if (updateWorkTime) {
            updatedSession?.let {
                finishedSessionsHandler.updateWorkTime(it)
            }
            // Skipping a running session
        } else {
            skippedSession?.let {
                finishedSessionsHandler.saveSession(it)
            }
        }

        if (isWork && !isFinished) {
            incrementStreak()
        }

        _timerData.value = _timerData.value.reset()

        val nextType = if (isWork) {
            if (shouldConsiderStreak(timeProvider.elapsedRealtime())) {
                TimerType.LONG_BREAK
            } else {
                TimerType.BREAK
            }
        } else {
            TimerType.WORK
        }
        log.v { "Next: $nextType" }
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
        _timerData.value = data.copy(
            state = TimerState.FINISHED
        )
        log.v { "Finish: $data" }
        val session = createFinishedSession()

        session?.let {
            finishedSessionsHandler.saveSession(it)
        } ?: log.e { "Should not happen: finished session was shorter than 1 minute" }

        if (data.type == TimerType.WORK) {
            incrementStreak()
        }

        listeners.forEach { it.onEvent(Event.Finished) }
        //TODO -toggle fullscreen
        // -toggle dnd mode
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
        log.v { "Reset: $data" }

        val isFinished = data.state == TimerState.FINISHED
        val isWork = data.type == TimerType.WORK

        val session =
            if (isFinished && !updateWorkTime) null else createFinishedSession()
        session?.let {
            if (updateWorkTime) {
                finishedSessionsHandler.updateWorkTime(it)
            } else {
                finishedSessionsHandler.saveSession(it)
            }
        }
        if (isWork && !isFinished) {
            incrementStreak()
        }
        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.value = _timerData.value.reset()
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
        val data = timerData.value.longBreakData
        val lastWorkEndTime = timeProvider.elapsedRealtime()
        val newData = data.copy(
            streak = data.streak + 1,
            lastWorkEndTime = lastWorkEndTime
        )
        _timerData.value = timerData.value.copy(longBreakData = newData)
        streakAndLongBreakHandler.incrementStreak(lastWorkEndTime)
    }

    //TODO: call this from the view when visible to make sure we have the latest data
    fun resetStreakIfNeeded(millis: Long = timeProvider.elapsedRealtime()) {
        if (!didLastWorkSessionFinishRecently(millis)) {
            val newLongBreakData = timerData.value.longBreakData.copy(
                streak = 0,
                lastWorkEndTime = 0L
            )
            _timerData.value = _timerData.value.copy(longBreakData = newLongBreakData)
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
        const val WIGGLE_ROOM_MILLIS = 100
    }
}