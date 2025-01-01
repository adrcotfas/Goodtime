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
package com.apps.adrcotfas.goodtime.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.Outline
import compose.icons.evaicons.fill.Home
import compose.icons.evaicons.fill.PieChart
import compose.icons.evaicons.fill.Settings
import compose.icons.evaicons.outline.Home
import compose.icons.evaicons.outline.PieChart
import compose.icons.evaicons.outline.Settings

sealed class Destination(val route: String, val label: String, val icon: ImageVector?, val selectedIcon: ImageVector? = null) {
    data object Main : Destination("main", "Main", icon = EvaIcons.Outline.Home, selectedIcon = EvaIcons.Fill.Home)
    data object Labels : Destination("labels", "Labels", icon = Icons.AutoMirrored.Outlined.Label, selectedIcon = Icons.AutoMirrored.Filled.Label)
    data object ArchivedLabels : Destination("archivedLabels", "Archived labels", null)
    data object Stats : Destination("stats", "Stats", icon = EvaIcons.Outline.PieChart, selectedIcon = EvaIcons.Fill.PieChart)
    data object Settings : Destination("settings", "Settings", icon = EvaIcons.Outline.Settings, selectedIcon = EvaIcons.Fill.Settings)

    data object GeneralSettings : Destination("generalSettings", "General Settings", null)
    data object TimerStyle : Destination("timerStyle", "Timer Style", null)
    data object NotificationSettings : Destination("notificationSettings", "Notifications", null)
    data object Backup : Destination("backup", "Backup", null)
    data object About : Destination("about", "About", null)
    data object Licenses : Destination("licenses", "Open Source Licenses", null)
}

val bottomNavigationItems = listOf(
    Destination.Main,
    Destination.Labels,
    Destination.Stats,
    Destination.Settings,
)
