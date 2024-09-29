package com.apps.adrcotfas.goodtime.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                        permissionsState.shouldAskForBatteryOptimizationRemoval,
                        permissionsState.shouldAskForBatteryOptimizationRemoval
                    ).count { it }
                    if (item.label == Destination.Settings.label && count > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    modifier = Modifier.padding(top = 2.dp),
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
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