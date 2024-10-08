package com.apps.adrcotfas.goodtime.main

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.BarChart2
import compose.icons.feathericons.Home
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Tag

sealed class Destination(val route: String, val label: String, val icon: ImageVector?) {
    data object Main : Destination("main", "Main", FeatherIcons.Home)
    data object Labels : Destination("labels", "Labels", FeatherIcons.Tag)
    data object ArchivedLabels : Destination("archivedLabels", "Archived labels", null)
    data object Stats : Destination("stats", "Stats", FeatherIcons.BarChart2)
    data object Settings : Destination("settings", "Settings", FeatherIcons.Settings)

    data object UserInterfaceSettings: Destination("userInterface", "User Interface", null)
    data object TimerStyle: Destination("timerStyle", "Timer Style", null)
    data object NotificationSettings : Destination("notificationSettings", "Notifications", null)
    data object Backup : Destination("backup", "Backup", null)
    data object About : Destination("about", "About", null)
    data object Licenses : Destination("licenses", "Open Source Licenses", null)
}

val bottomNavigationItems = listOf(
    Destination.Main,
    Destination.Labels,
    Destination.Stats,
    Destination.Settings
)