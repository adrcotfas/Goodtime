package com.apps.adrcotfas.goodtime.labels.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import kotlinx.coroutines.launch

class ArchivedLabelsViewModel(private val localDataRepository: LocalDataRepository) :
    ViewModel() {

    val archivedLabels = localDataRepository.selectAllLabelsArchived()

    fun unarchiveLabel(labelName: String) {
        viewModelScope.launch {
            localDataRepository.updateLabelIsArchived(labelName, false)
        }
    }

    fun deleteLabel(labelName: String) {
        viewModelScope.launch {
            localDataRepository.deleteLabel(labelName)
        }
    }
}