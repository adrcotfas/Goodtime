package com.apps.adrcotfas.goodtime.viewmodel

import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.endTime
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.domain.TimeProvider
import com.apps.adrcotfas.goodtime.domain.TimerManager
import com.apps.adrcotfas.goodtime.domain.TimerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class MainViewModel(
    private val timerManager: TimerManager,
) : ViewModel() {
    // expose flow for remaining time, currentSession and streak to be used in UI


    fun startTimer(type: TimerType) {
        timerManager.start(type)
    }
}