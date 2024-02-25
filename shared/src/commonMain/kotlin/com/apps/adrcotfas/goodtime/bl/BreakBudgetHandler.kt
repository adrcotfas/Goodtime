package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BreakBudgetHandler(
    private val coroutineScope: CoroutineScope,
    private val settingsRepo: SettingsRepository,
) {
    fun updateBreakBudget(data: BreakBudgetData) {
        coroutineScope.launch {
            settingsRepo.saveBreakBudgetData(data)
        }
    }
}