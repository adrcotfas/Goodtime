package com.apps.adrcotfas.goodtime.labels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.ui.lightPalette
import com.apps.adrcotfas.goodtime.utils.generateUniqueNameForDuplicate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LabelsUiState(
    val labels: List<Label> = emptyList(),
    val activeLabelName: String = Label.DEFAULT_LABEL_NAME,
    val archivedLabelCount: Int = 0,
    val showAddEditDialog: Boolean = false,
    val labelToEditInitialName: String = "",
    val labelToEdit: Label = Label.newLabelWithRandomColorIndex(lightPalette.lastIndex)
)

val LabelsUiState.unarchivedLabels: List<Label>
    get() = labels.filter { !it.isArchived }

val LabelsUiState.labelNames: List<String>
    get() = unarchivedLabels.map { it.name }

class LabelsViewModel(
    private val repo: LocalDataRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState = MutableStateFlow(LabelsUiState())

    init {
        viewModelScope.launch {
            repo.selectAllLabels().collect { labels ->
                uiState.update {
                    it.copy(
                        labels = labels,
                        archivedLabelCount = labels.filter { label -> label.isArchived }.size
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.map { settings -> settings.labelName }
                .collect { labelName ->
                    uiState.update { it.copy(activeLabelName = labelName) }
                }
        }
    }

    fun addLabel(label: Label) {
        viewModelScope.launch {
            repo.insertLabel(label)
        }
    }

    fun updateLabel(labelName: String, label: Label) {
        viewModelScope.launch {
            repo.updateLabel(labelName, label)
        }
    }

    fun deleteLabel(labelName: String) {
        viewModelScope.launch {
            val isDeletingActiveLabel = labelName == uiState.value.activeLabelName
            repo.deleteLabel(labelName)
            if (isDeletingActiveLabel) {
                settingsRepository.activateDefaultLabel()
            }
        }
    }

    fun setArchived(labelName: String, isArchived: Boolean) {
        viewModelScope.launch {
            val isActiveLabel = uiState.value.activeLabelName == labelName
            repo.updateLabelIsArchived(labelName, isArchived)
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
            repo.bulkUpdateLabelOrderIndex(labelsToUpdate)
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
                        insertLabelAt(newLabel, index + 1)
                    }
                })
            }
        }
    }

    private fun insertLabelAt(label: Label, index: Int) {
        viewModelScope.launch {
            uiState.update { state ->
                state.copy(labels = state.labels.toMutableList().apply {
                    add(index, label)
                })
            }
            repo.insertLabelAndBulkRearrange(
                label,
                uiState.value.unarchivedLabels.mapIndexed { index, label ->
                    Pair(label.name, index.toLong())
                }
            )
        }
    }

    fun setShowAddEditDialog(show: Boolean) {
        uiState.update { it.copy(showAddEditDialog = show) }
    }

    fun setShowAddEditDialog(show: Boolean, labelToEdit: Label) {
        uiState.update {
            it.copy(
                showAddEditDialog = show,
                labelToEditInitialName = labelToEdit.name,
                labelToEdit = labelToEdit
            )
        }
    }

    fun updateLabelToEdit(label: Label) {
        uiState.update {
            it.copy(labelToEdit = label)
        }
    }

    fun resetLabelToEdit() {
        uiState.update {
            it.copy(
                labelToEditInitialName = "",
                labelToEdit = Label.newLabelWithRandomColorIndex(lightPalette.lastIndex)
            )
        }
    }
}