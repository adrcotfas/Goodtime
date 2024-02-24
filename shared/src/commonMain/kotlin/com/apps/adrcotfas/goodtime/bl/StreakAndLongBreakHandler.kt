package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

class StreakAndLongBreakHandler(
    private val coroutineScope: CoroutineScope,
    private val settingsRepo: SettingsRepository
) {
    fun incrementStreak(lastWorkEndTime: Long) {
        coroutineScope.launch {
            val data = settingsRepo.settings.last().longBreakData
            val newData = data.copy(
                streak = data.streak + 1,
                lastWorkEndTime = lastWorkEndTime
            )
            settingsRepo.saveLongBreakData(newData)
        }
    }

    fun resetStreak() {
        coroutineScope.launch {
            val data = settingsRepo.settings.last().longBreakData
            val newData = data.copy(
                streak = 0,
                lastWorkEndTime = 0L
            )
            settingsRepo.saveLongBreakData(newData)
        }
    }
}