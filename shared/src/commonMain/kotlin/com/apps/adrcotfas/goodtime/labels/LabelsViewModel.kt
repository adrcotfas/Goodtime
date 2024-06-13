package com.apps.adrcotfas.goodtime.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.utils.generateUniqueNameForDuplicate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LabelsUiState(
    val labels: List<Label> = emptyList(),
    val activeLabelName: String = Label.DEFAULT_LABEL_NAME
)

val LabelsUiState.unarchivedLabels: List<Label>
    get() = labels.filter { !it.isArchived }

class LabelsViewModel(
    private val localDataRepository: LocalDataRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState = MutableStateFlow(LabelsUiState())

    init {
        viewModelScope.launch {
            localDataRepository.selectAllLabels().collect { labels ->
                uiState.update { it.copy(labels = labels) }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.map { settings -> settings.labelName }
                .collect { labelName ->
                    uiState.update { it.copy(activeLabelName = labelName) }
                }
        }
    }

    //TODO: use this when creating new labels
    fun addLabel(label: Label) {
        addLabel(label)
    }

    private fun addLabel(label: Label, index: Int? = null) {
        viewModelScope.launch {
            uiState.update { state ->
                state.copy(labels = state.labels.toMutableList().apply {
                    if (index != null) {
                        add(index, label)
                    } else {
                        add(label)
                    }
                })
            }
            if (index != null) {
                localDataRepository.insertLabelAndBulkRearrange(
                    label,
                    uiState.value.unarchivedLabels.mapIndexed { index, label ->
                        Pair(label.name, index.toLong())
                    }
                )
            } else {
                localDataRepository.insertLabel(label)
            }
        }
    }

    fun deleteLabel(labelName: String) {
        viewModelScope.launch {
            val isDeletingActiveLabel = labelName == uiState.value.activeLabelName
            localDataRepository.deleteLabel(labelName)
            if (isDeletingActiveLabel) {
                settingsRepository.activateDefaultLabel()
            }
        }
    }

    fun setArchived(labelName: String, isArchived: Boolean) {
        viewModelScope.launch {
            val isActiveLabel = uiState.value.activeLabelName == labelName
            localDataRepository.updateLabelIsArchived(labelName, isArchived)
            if (isActiveLabel) {
                settingsRepository.activateDefaultLabel()
            }
        }
    }

    fun setActiveLabel(labelName: String) {
        viewModelScope.launch {
            settingsRepository.activateLabelWithName(labelName)
        }
    }

    fun rearrangeLabel(fromIndex: Int, toIndex: Int) {
        uiState.update {
            it.copy(labels = it.unarchivedLabels.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            })
        }
    }

    fun rearrangeLabelsToDisk() {
        viewModelScope.launch {
            val labelsToUpdate = uiState.value.unarchivedLabels.mapIndexed { index, label ->
                Pair(label.name, index.toLong())
            }
            localDataRepository.bulkUpdateLabelOrderIndex(labelsToUpdate)
        }
    }

    fun duplicateLabel(name: String, isDefault: Boolean = false) {
        viewModelScope.launch {
            uiState.update { uiState ->
                uiState.copy(labels = uiState.labels.toMutableList().apply {
                    val index =
                        indexOfFirst { it.name == if (isDefault) Label.DEFAULT_LABEL_NAME else name }
                    if (index != -1) {
                        val label = get(index)
                        val newLabelName = generateUniqueNameForDuplicate(name, map { it.name })
                        val newLabel = label.copy(name = newLabelName)
                        addLabel(newLabel, index + 1)
                    }
                })
            }
        }
    }
}