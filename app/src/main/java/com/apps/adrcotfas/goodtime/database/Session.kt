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
package com.apps.adrcotfas.goodtime.database

import androidx.annotation.Nullable
import androidx.room.*
import com.apps.adrcotfas.goodtime.util.millis
import java.time.LocalDateTime

@Entity(
    foreignKeys = [ForeignKey(
        entity = Label::class,
        parentColumns = ["title", "archived"],
        childColumns = ["label", "archived"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
class Session(
    @field:PrimaryKey(autoGenerate = true)var id: Long,
    var timestamp: Long,
    var duration: Int,
    @Nullable
    var label: String?
) {
    @ColumnInfo(defaultValue = "0")
    var archived: Boolean = false

    @Ignore
    constructor() : this(0, LocalDateTime.now().millis, 0, null)
}
