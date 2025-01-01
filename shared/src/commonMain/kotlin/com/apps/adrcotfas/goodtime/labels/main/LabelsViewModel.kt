/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.labels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.ui.lightPalette
import com.apps.adrcotfas.goodtime.utils.generateUniqueNameForDuplicate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LabelsUiState(
    val isLoading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val activeLabelName: String = Label.DEFAULT_LABEL_NAME,
    val archivedLabelCount: Int = 0,

    val defaultLabelDisplayName: String = "",
    val labelToEdit: Label? = null, // this does not change after initialization
    val newLabel: Label = Label.newLabelWithRandomColorIndex(lightPalette.lastIndex),
)

val LabelsUiState.existingLabelNames: List<String>
    get() = labels.map { label -> label.name }

val LabelsUiState.archivedLabels: List<Label>
    get() = labels.filter { it.isArchived }

fun LabelsUiState.labelNameIsValid(): Boolean {
    val name = newLabel.name
    return name.isNotEmpty() && !existingLabelNames.map { labels -> labels.lowercase() }
        .minus(labelToEdit?.name?.lowercase())
        .contains(name.lowercase()) && name.lowercase() != defaultLabelDisplayName.lowercase()
}

val LabelsUiState.unarchivedLabels: List<Label>
    get() = labels.filter { !it.isArchived }

class LabelsViewModel(
    private val repo: LocalDataRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabelsUiState())
    val uiState = _uiState.onStart {
        loadData()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LabelsUiState())

    fun init(labelToEditName: String? = null, defaultLabelName: String) {
        val labelToEdit = labelToEditName?.let { name ->
            uiState.value.labels.find { label -> label.name == name }
        }
        _uiState.update {
            it.copy(
                defaultLabelDisplayName = defaultLabelName,
                labelToEdit = labelToEdit,
                newLabel = labelToEdit ?: Label.newLabelWithRandomColorIndex(lightPalette.lastIndex),
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            settingsRepository.settings.map { settings -> settings.labelName }
                .combine(repo.selectAllLabels()) { activeLabelName, labels ->
                    activeLabelName to labels
                }.distinctUntilChanged()
                .collect { (activeLabelName, labels) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeLabelName = activeLabelName,
                            labels = labels,
                            archivedLabelCount = labels.filter { label -> label.isArchived }.size,
                        )
                    }
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
            val isRenamingActiveLabel =
                labelName == _uiState.value.activeLabelName && labelName != label.name
            if (isRenamingActiveLabel) {
                settingsRepository.activateLabelWithName(label.name)
            }
            _uiState.update {
                it.copy(labelToEdit = label)
            }
        }
    }

    fun deleteLabel(labelName: String) {
        viewModelScope.launch {
            val isDeletingActiveLabel = labelName == _uiState.value.activeLabelName
            repo.deleteLabel(labelName)
            if (isDeletingActiveLabel) {
                settingsRepository.activateDefaultLabel()
            }
        }
    }

    fun setArchived(labelName: String, isArchived: Boolean) {
        viewModelScope.launch {
            val isActiveLabel = _uiState.value.activeLabelName == labelName
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
        _uiState.update {
            it.copy(
                labels = it.unarchivedLabels.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                },
            )
        }
    }

    fun rearrangeLabelsToDisk() {
        viewModelScope.launch {
            val labelsToUpdate = _uiState.value.unarchivedLabels.mapIndexed { index, label ->
                Pair(label.name, index.toLong())
            }
            if (labelsToUpdate.size > 1) {
                repo.bulkUpdateLabelOrderIndex(labelsToUpdate)
            }
        }
    }

    fun duplicateLabel(name: String, isDefault: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(
                    labels = uiState.labels.toMutableList().apply {
                        val index =
                            indexOfFirst { it.name == if (isDefault) Label.DEFAULT_LABEL_NAME else name }
                        if (index != -1) {
                            val label = get(index)
                            val newLabelName = generateUniqueNameForDuplicate(name, map { it.name })
                            val newLabel = label.copy(name = newLabelName)
                            insertLabelAt(newLabel, index + 1)
                        }
                    },
                )
            }
        }
    }

    private fun insertLabelAt(label: Label, index: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    labels = state.labels.toMutableList().apply {
                        add(index, label)
                    },
                )
            }
            repo.insertLabelAndBulkRearrange(
                label,
                _uiState.value.unarchivedLabels.mapIndexed { index, label ->
                    Pair(label.name, index.toLong())
                },
            )
        }
    }

    fun setNewLabel(newLabel: Label) {
        _uiState.update {
            it.copy(newLabel = newLabel)
        }
    }
}
