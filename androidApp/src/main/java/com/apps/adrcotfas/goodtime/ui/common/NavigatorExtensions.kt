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
@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator

val ThreePaneScaffoldNavigator<Any>.content: Any?
    get() = currentDestination?.content

val ThreePaneScaffoldNavigator<Any>.pane: ThreePaneScaffoldRole?
    get() = currentDestination?.pane

fun <T> ThreePaneScaffoldNavigator<T>.navigateToDetail(destination: T?) {
    navigateTo(
        pane = ListDetailPaneScaffoldRole.Detail,
        content = destination,
    )
}

fun <T> ThreePaneScaffoldNavigator<T>.navigateToExtra(destination: T) {
    navigateTo(
        pane = ListDetailPaneScaffoldRole.Extra,
        content = destination,
    )
}

fun <T> ThreePaneScaffoldNavigator<T>.isContent(content: T): Boolean {
    return currentDestination?.content == content
}
