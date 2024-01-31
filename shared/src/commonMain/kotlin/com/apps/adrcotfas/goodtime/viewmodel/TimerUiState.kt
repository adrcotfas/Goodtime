package com.apps.adrcotfas.goodtime.viewmodel

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.PersistedTimerData
import com.apps.adrcotfas.goodtime.domain.TimerState
import com.apps.adrcotfas.goodtime.domain.TimerType

data class TimerUiState(
    val lastStartTime: Long,
    val endTime: Long,
    val state: TimerState,
    val type: TimerType,
    val label: Label,
    val persistedTimerData: PersistedTimerData
)