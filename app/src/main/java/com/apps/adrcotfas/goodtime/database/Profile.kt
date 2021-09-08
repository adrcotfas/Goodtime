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

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Profile {
    @Ignore
    constructor(
        name: String, durationWork: Int, durationBreak: Int
    ) {
        this.name = name
        this.durationWork = durationWork
        this.durationBreak = durationBreak
        enableLongBreak = false
        durationLongBreak = 15
        sessionsBeforeLongBreak = 4
    }

    constructor(
        name: String,
        durationWork: Int,
        durationBreak: Int,
        durationLongBreak: Int,
        sessionsBeforeLongBreak: Int
    ) {
        this.name = name
        this.durationWork = durationWork
        this.durationBreak = durationBreak
        enableLongBreak = true
        this.durationLongBreak = durationLongBreak
        this.sessionsBeforeLongBreak = sessionsBeforeLongBreak
    }

    @PrimaryKey
    var name: String
    var durationWork: Int
    var durationBreak: Int
    var enableLongBreak: Boolean
    var durationLongBreak: Int
    var sessionsBeforeLongBreak: Int
}