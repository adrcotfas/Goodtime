package com.apps.adrcotfas.goodtime.bl

import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.minutes

/**
 * Manages the timer state and provides methods to start, pause, resume and finish the timer.
 */
class TimerManager(
    private val localDataRepo: LocalDataRepository,
    private val settingsRepo: SettingsRepository,
    private val listeners: List<EventListener>,
    private val timeProvider: TimeProvider,
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
        listeners.forEach { it.onEvent(Event.Start) }
        //TODO:
        // -alarm manager start
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
        listeners.forEach { it.onEvent(Event.AddOneMinute) }
        //TODO:
        // - alarm manager cancel, alarm manager reschedule
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
        //TODO:
        // - alarm manager cancel
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
        listeners.forEach { it.onEvent(Event.Start) }
        //TODO:
        // - alarm manager start
    }

    fun next() {
        if (_timerData.value.state == TimerState.RESET) {
            log.e { "Trying to start the next session when the timer is reset" }
            return
        }
        //TODO: save to stats if session is longer than 1 minute
        val sessionType = _timerData.value.type
        val state = _timerData.value.state
        if (state == TimerState.RUNNING || state == TimerState.PAUSED) {
            _timerData.value = _timerData.value.reset()
        }
        //TODO: compute if we need a long break
        log.v { "Next: ${timerData.value}" }
        start(if (sessionType == TimerType.WORK) TimerType.BREAK else TimerType.WORK)
    }

    fun finish() {
        if (_timerData.value.state == TimerState.RESET || _timerData.value.state == TimerState.FINISHED) {
            log.e { "Trying to finish the timer when it is reset or finished" }
            return
        }
        //TODO: save to stats if session is longer than 1 minute
        _timerData.value = _timerData.value.copy(
            state = TimerState.FINISHED
        )
        log.v { "Finish: ${timerData.value}" }
        listeners.forEach { it.onEvent(Event.Finished) }
    }

    fun reset() {
        log.v { "Reset: ${timerData.value}" }
        //TODO: save to stats if session is longer than 1 minute
        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.value = _timerData.value.reset()
        //TODO: cancel alarm
        // -toggle fullscreen
        // -toggle dnd mode
    }
}