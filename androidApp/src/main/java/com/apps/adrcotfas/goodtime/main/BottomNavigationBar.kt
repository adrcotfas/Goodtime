package com.apps.adrcotfas.goodtime.main

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.apps.adrcotfas.goodtime.settings.permissions.getPermissionsState

@Composable
fun BottomNavigationBar(navController: NavController) {

    val permissionsState = getPermissionsState()

    BottomAppBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    val count = listOf(
                        permissionsState.shouldAskForNotificationPermission,
                        permissionsState.shouldAskForBatteryOptimizationRemoval
                    ).count { it }
                    if (item.label == Destination.Settings.label && count > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ) {
                                    Text(text = count.toString())
                                }
                            }
                        ) { Icon(item.icon!!, contentDescription = null) }
                    } else {
                        Icon(item.icon!!, contentDescription = null)
                    }
                },
            )
        }
    }
}