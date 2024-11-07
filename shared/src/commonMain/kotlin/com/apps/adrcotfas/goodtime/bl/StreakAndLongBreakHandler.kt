package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StreakAndLongBreakHandler(
    private val coroutineScope: CoroutineScope,
    private val settingsRepo: SettingsRepository
) {
    fun incrementStreak(lastWorkEndTime: Long) {
        coroutineScope.launch {
            val data = settingsRepo.settings.first().longBreakData
            val newData = data.copy(
                streak = data.streak + 1,
                lastWorkEndTime = lastWorkEndTime
            )
            settingsRepo.saveLongBreakData(newData)
        }
    }

    fun resetStreak() {
        coroutineScope.launch {
            settingsRepo.saveLongBreakData(LongBreakData())
        }
    }
}