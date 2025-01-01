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
package com.apps.adrcotfas.goodtime.data.model

import kotlin.random.Random

data class Label(
    // TODO: do I need the id here?
    val id: Long = 0,
    val name: String,
    val colorIndex: Long = 24,
    val orderIndex: Long = Long.MAX_VALUE,
    val useDefaultTimeProfile: Boolean = true,
    /** the real profile to be used is determined by [useDefaultTimeProfile], if true, use the default profile **/
    val timerProfile: TimerProfile = TimerProfile(),
    val isArchived: Boolean = false,
) {
    companion object {
        const val DEFAULT_LABEL_NAME = "PRODUCTIVITY_DEFAULT_LABEL"
        const val DEFAULT_LABEL_COLOR_INDEX = 24L
        const val LABEL_NAME_MAX_LENGTH = 32
        fun defaultLabel() =
            Label(name = DEFAULT_LABEL_NAME, colorIndex = DEFAULT_LABEL_COLOR_INDEX, orderIndex = 0)

        fun newLabelWithRandomColorIndex(lastIndex: Int) =
            Label(name = "", colorIndex = Random.nextInt(lastIndex).toLong())
    }

    fun isSameAs(label: Label): Boolean {
        return this.copy(id = 0, orderIndex = 0) == label.copy(id = 0, orderIndex = 0)
    }
}

fun Label.isDefault() = name == Label.DEFAULT_LABEL_NAME
