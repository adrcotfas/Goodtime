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

    //TODO: call this in a global scope before the apps starts?
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

    fun start(timerType: TimerType) {
        if (_timerData.value.label == null) {
            //TODO: log error
        }
        val now = timeProvider.now()
        _timerData.value = _timerData.value.copy(
            startTime = now,
            lastStartTime = now,
            endTime = _timerData.value.label!!.timerProfile.endTime(timerType, now),
            type = timerType,
            state = TimerState.RUNNING,
            minutesAdded = 0
        )
        listeners.forEach { it.onEvent(Event.StartEvent(_timerData.value)) }
        //TODO:
        // -alarm manager start
        // -notification start
        // -toggle fullscreen
        // -toggle dnd mode
    }

    fun addOneMinute() {
        val data = _timerData.value
        _timerData.value = data.copy(
            endTime = data.endTime + 1.minutes.inWholeMilliseconds,
            minutesAdded = data.minutesAdded + 1
        )
        listeners.forEach { it.onEvent(Event.AddOneMinute) }
        //TODO:
        // - alarm manager cancel, alarm manager reschedule
        // - notification update
    }

    fun pause() {
        val data = _timerData.value
        _timerData.value = data.copy(
            tmpRemaining = data.endTime - timeProvider.now(),
            endTime = 0,
            state = TimerState.PAUSED
        )
        listeners.forEach { it.onEvent(Event.PauseEvent) }
        //TODO:
        // - alarm manager cancel
        // - notification update
    }

    fun resume() {
        val data = _timerData.value
        val now = timeProvider.now()
        _timerData.value = data.copy(
            lastStartTime = now,
            endTime = data.tmpRemaining + now,
            state = TimerState.RUNNING,
            tmpRemaining = 0
        )
        listeners.forEach { it.onEvent(Event.StartEvent(_timerData.value)) }
        //TODO:
        // - alarm manager start
        // - notification update
    }

    fun finish() {
        _timerData.value = _timerData.value.copy(
            state = TimerState.FINISHED
        )
        listeners.forEach { it.onEvent(Event.Finished) }
        //TODO: notification update(with actions)
    }

    fun reset() {
        listeners.forEach { it.onEvent(Event.Reset(_timerData.value)) }
        _timerData.value = DomainTimerData()
        //TODO: alarm manager cancel, notification cancel
        // -toggle fullscreen
        // -toggle dnd mode
    }
}