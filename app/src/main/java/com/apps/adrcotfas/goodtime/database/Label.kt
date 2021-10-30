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

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["title", "archived"])
class Label(
    @ColumnInfo(defaultValue = "")
    @NonNull
    var title: String,

    @ColumnInfo(defaultValue = "0")
    @NonNull
    var colorId: Int) {

    @ColumnInfo(defaultValue = "0")
    @NonNull
    var order: Int= 0

    @ColumnInfo(defaultValue = "0")
    @NonNull
    var archived: Boolean = false
}