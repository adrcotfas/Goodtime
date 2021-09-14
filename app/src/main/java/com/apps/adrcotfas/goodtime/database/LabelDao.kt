/*
 * Copyright 2016-2021 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.database

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addLabel(label: Label)

    @get:Query("select * from Label where archived = 0 or archived = NULL ORDER BY `order`")
    val labels: LiveData<List<Label>>

    @get:Query("select * from Label ORDER BY `order`")
    val allLabels: LiveData<List<Label>>

    @Query("select colorId from Label where title = :title")
    fun getColor(title: String): LiveData<Int>

    @Query("update Label SET title = :newTitle WHERE title = :title")
    suspend fun editLabelName(title: String, newTitle: String?)

    @Query("update Label SET colorId = :colorId WHERE title = :title")
    suspend fun editLabelColor(title: String, colorId: Int)

    @Query("update Label SET `order` = :order WHERE title = :title")
    suspend fun editLabelOrder(title: String, order: Int)

    @Query("delete from Label where title = :title")
    suspend fun deleteLabel(title: String)

    @Query("update Label SET archived = :archived WHERE title = :title")
    suspend fun toggleLabelArchiveState(title: String, archived: Boolean)
}