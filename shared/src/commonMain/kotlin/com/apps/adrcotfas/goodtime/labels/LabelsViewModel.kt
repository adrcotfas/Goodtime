package com.apps.adrcotfas.goodtime.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LabelsViewModel(
    private val localDataRepository: LocalDataRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val labels = localDataRepository.selectAllLabels().stateIn(
        viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val activeLabel = settingsRepository.settings.map { it.labelName }.stateIn(
        viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    fun addLabel(label: Label) {
        viewModelScope.launch {
            localDataRepository.insertLabel(label)
        }
    }

    fun deleteLabel(labelName: String) {
        viewModelScope.launch {
            localDataRepository.deleteLabel(labelName)
            if (labelName == activeLabel.value) {
                settingsRepository.activateDefaultLabel()
            }
        }
    }

    fun setActiveLabel(labelName: String) {
        viewModelScope.launch {
            settingsRepository.activateLabelWithName(labelName)
        }
    }
}