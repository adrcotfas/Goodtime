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

import com.apps.adrcotfas.goodtime.database.AppDatabase.Companion.getDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.apps.adrcotfas.goodtime.database.LabelDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.statistics.Utils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LabelsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: LabelDao = getDatabase(application).labelDao()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

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
        executorService.execute { dao.addLabel(label) }
    }

    fun editLabelName(label: String, newLabel: String?) {
        executorService.execute { dao.editLabelName(label, newLabel) }
    }

    fun editLabelColor(label: String, color: Int) {
        executorService.execute { dao.editLabelColor(label, color) }
    }

    fun editLabelOrder(label: String, newOrder: Int) {
        executorService.execute { dao.editLabelOrder(label, newOrder) }
    }

    fun toggleLabelArchive(label: String, archived: Boolean) {
        executorService.execute { dao.toggleLabelArchiveState(label, archived) }
    }

    fun deleteLabel(label: String) {
        executorService.execute { dao.deleteLabel(label) }
    }

    init {
        crtExtendedLabel.value =
            Utils.getInstanceTotalLabel(application.baseContext)
    }
}