package com.apps.adrcotfas.goodtime

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.apps.adrcotfas.goodtime.settings.user_interface.UserInterfaceScreen
import com.apps.adrcotfas.goodtime.stats.StatsScreen

@Composable
fun GoodtimeNavHost(
    innerPadding: PaddingValues,
    navController: NavHostController
) {

    val backToMainSettings = {
        navController.navigate(Destination.Settings.route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    NavHost(
        modifier = Modifier
            .padding(innerPadding),
        navController = navController,
        startDestination = Destination.Main.route
    ) {
        composable(Destination.Main.route) { MainScreen() }
        composable(Destination.Labels.route) {
            LabelsScreen(navController)
        }
        composable(Destination.ArchivedLabels.route) {
            ArchivedLabelsScreen({
                navController.navigate(Destination.Labels.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            })
        }

        composable(Destination.Stats.route) { StatsScreen() }
        composable(Destination.Settings.route) { SettingsScreen(navController = navController) }
        composable(Destination.UserInterfaceSettings.route) {
            UserInterfaceScreen(onNavigateBack = backToMainSettings)
        }
        composable(Destination.NotificationSettings.route) {
            NotificationsScreen(onNavigateBack = backToMainSettings)
        }
        composable(Destination.Backup.route) {
            BackupScreen(onNavigateBack = backToMainSettings)
        }
        composable(Destination.About.route) {
            AboutScreen(onNavigateBack = backToMainSettings, onNavigateToLicenses = {
                navController.navigate(Destination.Licenses.route)
            })
        }
        composable(Destination.Licenses.route) {
            LicensesScreen {
                navController.navigate(Destination.About.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }
}