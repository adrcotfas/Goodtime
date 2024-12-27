package com.apps.adrcotfas.goodtime

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.apps.adrcotfas.goodtime.labels.main.LabelsScreen
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.main.MainScreen
import com.apps.adrcotfas.goodtime.settings.SettingsScreen
import com.apps.adrcotfas.goodtime.stats.StatsScreen


private fun NavHostController.backTo(route: String): () -> Unit = {
    navigate(route) {
        popUpTo(graph.startDestinationId)
        launchSingleTop = true
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Main.route
    ) {
        composable(Destination.Main.route) { MainScreen() }
        composable(Destination.Labels.route) { LabelsScreen() }
        composable(Destination.Stats.route) { StatsScreen() }
        composable(Destination.Settings.route) { SettingsScreen() }
    }
}