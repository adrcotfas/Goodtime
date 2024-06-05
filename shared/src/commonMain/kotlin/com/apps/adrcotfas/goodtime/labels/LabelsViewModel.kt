package com.apps.adrcotfas.goodtime.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LabelsUiState(
    val labels: List<Label> = emptyList(),
    val activeLabelName: String = Label.DEFAULT_LABEL_NAME
)

// All of this extra code (UI state related) that practically duplicates the logic of adding, deleting and rearranging labels to/from the repository
// is required for only one thing: having a nice animation when dragging and dropping labels.
// This is not ideal but it will do for now until I find a way to interact only with the flow coming from the repository.

class LabelsViewModel(
    private val localDataRepository: LocalDataRepository,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    val uiState = MutableStateFlow(LabelsUiState())

    init {
        coroutineScope.launch {
            uiState.update { it.copy(labels = localDataRepository.selectAllLabels().first()) }
        }
        coroutineScope.launch {
            uiState.update {
                it.copy(activeLabelName = settingsRepository.settings.map { settings -> settings.labelName }
                    .first())
            }
        }
    }

    fun addLabel(label: Label) {
        viewModelScope.launch {
            uiState.update { state ->
                state.copy(labels = state.labels + label)
            }
            localDataRepository.insertLabel(label)
        }
    }

    fun deleteLabel(labelName: String) {
        viewModelScope.launch {
            val isDeletingActiveLabel = labelName == uiState.value.activeLabelName
            uiState.update { state ->
                state.copy(
                    labels = if (labelName != Label.DEFAULT_LABEL_NAME) state.labels.filter { it.name != labelName } else state.labels,
                    activeLabelName =
                    if (isDeletingActiveLabel) {
                        Label.DEFAULT_LABEL_NAME
                    } else {
                        state.activeLabelName
                    }
                )
            }
            localDataRepository.deleteLabel(labelName)
            if (isDeletingActiveLabel) {
                settingsRepository.activateDefaultLabel()
            }
        }
    }

    fun setActiveLabel(labelName: String) {
        viewModelScope.launch {
            uiState.update { state ->
                state.copy(activeLabelName = labelName)
            }
            settingsRepository.activateLabelWithName(labelName)
        }
    }

    fun rearrangeLabel(fromIndex: Int, toIndex: Int) {
        uiState.update {
            it.copy(labels = it.labels.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            })
        }
        coroutineScope.launch {
            uiState.value.labels.map { it.name }.forEachIndexed { index, labelName ->
                localDataRepository.updateLabelOrderIndex(labelName, index.toLong())
            }
        }
    }
}