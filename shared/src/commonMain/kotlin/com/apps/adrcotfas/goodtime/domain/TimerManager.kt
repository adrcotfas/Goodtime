package com.apps.adrcotfas.goodtime.domain

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
) {

    private val _timerData: MutableStateFlow<DomainTimerData> = MutableStateFlow(DomainTimerData())
    val timerData: StateFlow<DomainTimerData> = _timerData

    suspend fun init() {
        settingsRepo.settings.map {
            it.persistedTimerData
        }.flatMapLatest {
            _timerData.value.persistedTimerData = it
            it.labelName?.let { labelName ->
                localDataRepo.selectLabelByName(labelName)
            } ?: localDataRepo.selectDefaultLabel()
        }.distinctUntilChanged().collect {
            _timerData.value = _timerData.value.copy(label = it)
        }
    }

    fun start(timerType: TimerType = _timerData.value.type) {
        if (_timerData.value.label == null) {
            //TODO: log error
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
        listeners.forEach { it.onEvent(Event.Start) }
        //TODO:
        // -alarm manager start
        // -toggle fullscreen
        // -toggle dnd mode
    }

    fun addOneMinute() {
        if (_timerData.value.state != TimerState.RUNNING) {
            //TODO: log error
            return
        }
        val data = _timerData.value
        _timerData.value = data.copy(
            endTime = data.endTime + 1.minutes.inWholeMilliseconds,
            minutesAdded = data.minutesAdded + 1
        )
        listeners.forEach { it.onEvent(Event.AddOneMinute) }
        //TODO:
        // - alarm manager cancel, alarm manager reschedule
    }

    fun pause() {
        if (_timerData.value.state != TimerState.RUNNING) {
            //TODO: log error
            return
        }
        val data = _timerData.value
        _timerData.value = data.copy(
            tmpRemaining = data.endTime - timeProvider.elapsedRealtime(),
            state = TimerState.PAUSED
        )
        listeners.forEach { it.onEvent(Event.Pause) }
        //TODO:
        // - alarm manager cancel
    }

    fun resume() {
        if (_timerData.value.state != TimerState.PAUSED) {
            //TODO: log error
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
        listeners.forEach { it.onEvent(Event.Start) }
        //TODO:
        // - alarm manager start
    }

    fun next() {
        if (_timerData.value.state == TimerState.RESET) {
            //TODO: log error
            return
        }
        //TODO: save to stats if session is longer than 1 minute
        val sessionType = _timerData.value.type
        val state = _timerData.value.state
        if (state == TimerState.RUNNING || state == TimerState.PAUSED) {
            _timerData.value = _timerData.value.reset()
        }
        //TODO: compute if we need a long break
        start(if (sessionType == TimerType.WORK) TimerType.BREAK else TimerType.WORK)
    }

    fun finish() {
        if (_timerData.value.state == TimerState.RESET || _timerData.value.state == TimerState.FINISHED) {
            //TODO: log error
            return
        }
        //TODO: save to stats if session is longer than 1 minute
        _timerData.value = _timerData.value.copy(
            state = TimerState.FINISHED
        )
        listeners.forEach { it.onEvent(Event.Finished) }
    }

    fun reset() {
        //TODO: save to stats if session is longer than 1 minute
        listeners.forEach { it.onEvent(Event.Reset) }
        _timerData.value = _timerData.value.reset()
        //TODO: cancel alarm
        // -toggle fullscreen
        // -toggle dnd mode
    }
}