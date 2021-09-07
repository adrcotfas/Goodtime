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

package com.apps.adrcotfas.goodtime.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public interface LabelDao {

    @Insert(onConflict = IGNORE)
    void addLabel(Label session);

    @Query("select * from Label where archived = 0 or archived = NULL ORDER BY `order`")
    LiveData<List<Label>> getLabels();

    @Query("select * from Label ORDER BY `order`")
    LiveData<List<Label>> getAllLabels();

    @Query("select colorId from Label where title = :title")
    LiveData<Integer> getColor(String title);

    @Query("update Label SET title = :newTitle WHERE title = :title")
    void editLabelName(String title, String newTitle);

    @Query("update Label SET colorId = :colorId WHERE title = :title")
    void editLabelColor(String title, int colorId);

    @Query("update Label SET `order` = :order WHERE title = :title")
    void editLabelOrder(String title, int order);

    @Query("delete from Label where title = :title")
    void deleteLabel(String title);

    @Query("update Label SET archived = :archived WHERE title = :title")
    void toggleLabelArchiveState(String title, boolean archived);
}
