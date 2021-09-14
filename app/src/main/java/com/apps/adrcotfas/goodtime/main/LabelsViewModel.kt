/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.main

import android.content.Context
import androidx.lifecycle.*
import com.apps.adrcotfas.goodtime.database.AppDatabase
import com.apps.adrcotfas.goodtime.database.LabelDao
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.statistics.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: AppDatabase
) : ViewModel() {

    private val dao: LabelDao = database.labelDao()

    /**
     * The current selected label in the Statistics view
     * "extended" because it might be "total" or "unlabeled"
     */
    val crtExtendedLabel = MutableLiveData<Label>()

    /**
     * Returns only the labels which are not archived
     */
    val labels: LiveData<List<Label>>
        get() = dao.labels

    /**
     * Returns all labels, including the archived ones
     */
    val allLabels: LiveData<List<Label>>
        get() = dao.allLabels

    fun getColorOfLabel(label: String): LiveData<Int> {
        return dao.getColor(label)
    }

    fun addLabel(label: Label) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.addLabel(label)
        }
    }

    fun editLabelName(label: String, newLabel: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabelName(label, newLabel)
        }
    }

    fun editLabelColor(label: String, color: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabelColor(label, color)
        }
    }

    fun editLabelOrder(label: String, newOrder: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabelOrder(label, newOrder)
        }
    }

    fun toggleLabelArchive(label: String, archived: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.toggleLabelArchiveState(label, archived)
        }
    }

    fun deleteLabel(label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteLabel(label)
        }
    }

    init {
        crtExtendedLabel.value =
            Utils.getInstanceTotalLabel(context)
    }
}