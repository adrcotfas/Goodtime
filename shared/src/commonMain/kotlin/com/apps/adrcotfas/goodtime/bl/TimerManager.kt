package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    private val log: Logger
) {

    private val _timerData: MutableStateFlow<DomainTimerData> = MutableStateFlow(DomainTimerData())
    val timerData: StateFlow<DomainTimerData> = _timerData

    suspend fun init() {
        log.v { "Initializing TimerManager..." }
        settingsRepo.settings.map {
            it.persistedTimerData
        }.flatMapLatest {
            _timerData.value.persistedTimerData = it
            log.v { "new persistedTimerData: $it" }
            it.labelName?.let { labelName ->
                localDataRepo.selectLabelByName(labelName)
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
                endTime = elapsedRealTime + _timerData.value.tmpRemaining,
                state = TimerState.RUNNING
            )
        } else {
            _timerData.value = _timerData.value.copy(
                startTime = elapsedRealTime,
                lastStartTime = elapsedRealTime,
                endTime = _timerData.value.label!!.timerProfile.endTime(timerType, elapsedRealTime),
                type = timerType,
                state = TimerState.RUNNING,
                minutesAdded = 0
            )
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
            minutesAdded = data.minutesAdded + 1
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
            tmpRemaining = data.endTime - timeProvider.elapsedRealtime(),
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
        _timerData.value = data.copy(
            lastStartTime = elapsedRealTime,
            endTime = data.tmpRemaining + elapsedRealTime,
            state = TimerState.RUNNING,
            tmpRemaining = 0
        )
        log.v { "Resumed: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Start(timerData.value.endTime)) }
    }

    fun next(updateWorkTime: Boolean = false) {
        val data = timerData.value
        val state = data.state
        val type = data.type
        if (state == TimerState.RESET) {
            log.e { "Trying to start the next session but the timer is reset" }
            return
        }

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

        _timerData.value = _timerData.value.reset()
        //TODO: compute if we need a long break
        val nextType = if (type == TimerType.WORK) TimerType.BREAK else TimerType.WORK
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
        //TODO: update streak
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
        val session =
            if (data.state == TimerState.FINISHED && !updateWorkTime) null else createFinishedSession()
        session?.let {
            if (updateWorkTime) {
                finishedSessionsHandler.updateWorkTime(it)
            } else {
                finishedSessionsHandler.saveSession(it)
            }
        }
        //TODO: update streak
        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.value = _timerData.value.reset()
    }

    private fun createFinishedSession(): Session? {
        val data = timerData.value
        val isWork = data.type == TimerType.WORK

        val totalDuration = timeProvider.elapsedRealtime() - data.startTime

        val durationToSave = if (isWork) {
            val timeAddedManually = data.minutesAdded.minutes.inWholeMilliseconds
            val timeSpentPaused = totalDuration - data.getDuration() - timeAddedManually
            val justWorkTime = (totalDuration - timeSpentPaused + 100).milliseconds.inWholeMinutes
            justWorkTime
        } else {
            totalDuration.milliseconds.inWholeMinutes
        }

        if (durationToSave < 1) {
            log.i { "The session was shorter than 1 minute" }
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
}