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
package com.apps.adrcotfas.goodtime.ui

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import com.apps.adrcotfas.goodtime.R

@OptIn(ExperimentalTextApi::class)
fun timerFontWith(resId: Int, weight: Int): FontFamily {
    return FontFamily(
        Font(
            resId = resId,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings((FontVariation.weight(weight))),
        ),
    )
}

val timerFontWeights = listOf(100, 200, 300)
val timerFontAzeretMap =
    timerFontWeights.associateWith { weight -> timerFontWith(R.font.azeret_mono, weight) }

val timerTextAzeretStyle = TextStyle(
    fontFamily = timerFontAzeretMap[100],
    fontSize = 60.em,
)
