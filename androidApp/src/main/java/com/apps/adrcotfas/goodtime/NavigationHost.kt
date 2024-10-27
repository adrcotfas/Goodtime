package com.apps.adrcotfas.goodtime

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.apps.adrcotfas.goodtime.labels.archived.ArchivedLabelsScreen
import com.apps.adrcotfas.goodtime.labels.main.LabelsScreen
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.main.MainScreen
import com.apps.adrcotfas.goodtime.settings.SettingsScreen
import com.apps.adrcotfas.goodtime.settings.about.AboutScreen
import com.apps.adrcotfas.goodtime.settings.about.LicensesScreen
import com.apps.adrcotfas.goodtime.settings.backup.BackupScreen
import com.apps.adrcotfas.goodtime.settings.notifications.NotificationsScreen
import com.apps.adrcotfas.goodtime.settings.user_interface.TimerStyleScreen
import com.apps.adrcotfas.goodtime.settings.user_interface.UserInterfaceScreen
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
    val backToSettings = navController.backTo(Destination.Settings.route)
    val backToAbout = navController.backTo(Destination.About.route)
    val backToUserInterfaceSettings = navController.backTo(Destination.UserInterfaceSettings.route)
    val backToLabels = navController.backTo(Destination.Labels.route)

    NavHost(
        navController = navController,
        startDestination = Destination.Main.route
    ) {
        composable(Destination.Main.route) { MainScreen() }
        composable(Destination.Labels.route) { LabelsScreen(navController) }
        composable(Destination.ArchivedLabels.route) { ArchivedLabelsScreen({ backToLabels() }) }
        composable(Destination.Stats.route) { StatsScreen() }
        composable(Destination.Settings.route) { SettingsScreen(navController = navController) }
        composable(Destination.UserInterfaceSettings.route) {
            UserInterfaceScreen(navController = navController, onNavigateBack = backToSettings)
        }
        composable(Destination.TimerStyle.route) { TimerStyleScreen(onNavigateBack = backToUserInterfaceSettings) }
        composable(Destination.NotificationSettings.route) { NotificationsScreen(onNavigateBack = backToSettings) }
        composable(Destination.Backup.route) { BackupScreen(onNavigateBack = backToSettings) }
        composable(Destination.About.route) {
            AboutScreen(onNavigateBack = backToSettings, onNavigateToLicenses = {
                navController.navigate(Destination.Licenses.route)
            })
        }
        composable(Destination.Licenses.route) { LicensesScreen { backToAbout() } }
    }
}