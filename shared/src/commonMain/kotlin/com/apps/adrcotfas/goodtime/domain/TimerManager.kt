package com.apps.adrcotfas.goodtime.domain

import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

/**
 * Manages the timer state and provides methods to start, pause, resume and finish the timer.
 * Wait for [isReady] to be true before calling any of the methods.
 */
class TimerManager(
    private val localDataRepo: LocalDataRepository,
    private val settingsRepo: SettingsRepository,
    private val listeners: List<EventListener>,
    private val timeProvider: TimeProvider,
    private val coroutineScope: CoroutineScope,
) {

    private var _timerData: MutableStateFlow<TimerData> = MutableStateFlow(TimerData())
    val timerData: StateFlow<TimerData> = _timerData

    private val _isReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        coroutineScope.launch {
            settingsRepo.settings.map {
                it.currentTimerData.labelName
            }.flatMapLatest { labelName ->
                labelName?.let {
                    localDataRepo.selectLabelByName(it)
                } ?: localDataRepo.selectDefaultLabel()
            }.distinctUntilChanged().collect {
                _timerData.value = _timerData.value.copy(label = it)
                _isReady.value = true
            }
        }
    }

    fun start(timerType: TimerType) {
        val now = timeProvider.now()
        _timerData.value = _timerData.value.copy(
            startTime = now,
            endTime = _timerData.value.label!!.timerProfile.endTime(timerType, now),
            type = timerType,
            state = TimerState.RUNNING,
            minutesAdded = 0
        )
        listeners.forEach { it.onEvent(Event.StartEvent(_timerData.value)) }
    }

    fun addOneMinute() {
        val data = _timerData.value
        _timerData.value = data.copy(
            endTime = data.endTime + 1.minutes.inWholeMilliseconds,
            minutesAdded = data.minutesAdded + 1
        )
        listeners.forEach { it.onEvent(Event.AddOneMinute) }
    }

    fun pause() {
        val data = _timerData.value
        _timerData.value = data.copy(
            tmpRemaining = data.endTime - timeProvider.now(),
            endTime = 0,
            state = TimerState.PAUSED
        )
        listeners.forEach { it.onEvent(Event.PauseEvent) }
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
    }

    fun finish() {
        _timerData.value = _timerData.value.copy(
            state = TimerState.FINISHED
        )
        listeners.forEach { it.onEvent(Event.Finished) }
    }

    fun reset() {
        val label = _timerData.value.label
        listeners.forEach { it.onEvent(Event.Reset(_timerData.value)) }
        _timerData.value = TimerData(label = label)
    }

    suspend fun setLabelName(labelName: String?) {
        val currentTimerData = settingsRepo.settings.stateIn(coroutineScope).value.currentTimerData
        settingsRepo.saveCurrentTimerData(currentTimerData.copy(labelName = labelName))
        listeners.forEach { listener -> listener.onEvent(Event.SetLabelName(labelName)) }
    }
}