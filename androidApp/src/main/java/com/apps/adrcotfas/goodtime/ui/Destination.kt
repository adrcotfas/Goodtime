package com.apps.adrcotfas.goodtime.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Main : Destination("main", "Main", Icons.Default.Home)
    data object Labels : Destination("labels", "Labels", Icons.Default.Place)
    data object Stats : Destination("stats", "Stats", Icons.Default.ThumbUp)
    data object Settings : Destination("settings", "Settings", Icons.Default.Settings)
}

val bottomNavigationItems = listOf(
    Destination.Main,
    Destination.Labels,
    Destination.Stats,
    Destination.Settings
)