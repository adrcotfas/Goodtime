package com.apps.adrcotfas.goodtime.domain

import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class TimerManager(
    private val localDataRepo: LocalDataRepository,
    private val settingsRepo: SettingsRepository,
    private val listeners: List<EventListener>,
    private val timeProvider: TimeProvider,
    private val coroutineScope: CoroutineScope,
) {

    private val _isReady = MutableStateFlow(false)
    val isReady: Flow<Boolean> = _isReady

    var timerData = TimerData()
        private set

    private lateinit var currentLabel: Label

    init {
        coroutineScope.launch {
            settingsRepo.settings.map {
                it.currentTimerData.labelName
            }.flatMapLatest { labelName ->
                labelName?.let {
                    localDataRepo.selectLabelByName(it)
                } ?: localDataRepo.selectDefaultLabel()
            }.distinctUntilChanged().collect {
                currentLabel = it
                _isReady.value = true
            }
        }
    }

    fun start(timerType: TimerType) {
        val now = timeProvider.now()
        timerData = timerData.copy(
            startTime = now,
            endTime = currentLabel.timerProfile.endTime(timerType, now),
            type = timerType,
            state = TimerState.RUNNING,
            minutesAdded = 0
        )
        listeners.forEach { it.onEvent(Event.StartEvent(timerData)) }
    }

    fun addOneMinute() {
        timerData = timerData.copy(
            endTime = timerData.endTime + 1.minutes.inWholeMilliseconds,
            minutesAdded = timerData.minutesAdded + 1
        )
        listeners.forEach { it.onEvent(Event.AddOneMinute) }
    }

    fun pause() {
        timerData = timerData.copy(
            tmpRemaining = timerData.endTime - timeProvider.now(),
            endTime = 0,
            state = TimerState.PAUSED
        )
        listeners.forEach { it.onEvent(Event.PauseEvent) }
    }

    fun resume() {
        val now = timeProvider.now()
        timerData = timerData.copy(
            lastStartTime = now,
            endTime = timerData.tmpRemaining + now,
            state = TimerState.RUNNING,
            tmpRemaining = 0
        )
        listeners.forEach { it.onEvent(Event.StartEvent(timerData)) }
    }

    fun finish() {
        timerData = timerData.copy(
            state = TimerState.FINISHED
        )
        listeners.forEach { it.onEvent(Event.Finished) }
    }

    fun reset() {
        listeners.forEach { it.onEvent(Event.Reset(timerData)) }
        timerData = TimerData()
    }

    suspend fun setLabelName(labelName: String?) {
        val currentTimerData = settingsRepo.settings.stateIn(coroutineScope).value.currentTimerData
        settingsRepo.saveCurrentTimerData(currentTimerData.copy(labelName = labelName))
        listeners.forEach { listener -> listener.onEvent(Event.SetLabelName(labelName)) }
    }
}