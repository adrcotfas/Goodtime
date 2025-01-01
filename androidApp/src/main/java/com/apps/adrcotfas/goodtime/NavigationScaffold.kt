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
package com.apps.adrcotfas.goodtime

import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.main.bottomNavigationItems
import com.apps.adrcotfas.goodtime.settings.permissions.getPermissionsState

@Composable
fun NavigationScaffold(
    showNavigation: Boolean,
    onNavigate: (route: String) -> Unit,
    currentDestination: String?,
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val permissionsState = getPermissionsState()

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val customNavSuiteType = with(adaptiveInfo) {
        if (!showNavigation) {
            NavigationSuiteType.None
        } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }

    val colors =
        if (dynamicColor) {
            NavigationSuiteDefaults.colors()
        } else {
            NavigationSuiteDefaults.colors(
                navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
        }

    NavigationSuiteScaffold(
        layoutType = customNavSuiteType,
        navigationSuiteColors = colors,
        navigationSuiteItems = {
            bottomNavigationItems.forEach {
                val isSelected = it.route == currentDestination
                val icon = if (isSelected) it.selectedIcon else it.icon
                item(
                    badge = {
                        val count = listOf(
                            permissionsState.shouldAskForNotificationPermission,
                            permissionsState.shouldAskForBatteryOptimizationRemoval,
                        ).count { state -> state }
                        if (it.label == Destination.Settings.label && count > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                            ) {
                                Text(text = count.toString())
                            }
                        }
                    },
                    icon = { Icon(icon!!, contentDescription = null) },
                    selected = isSelected,
                    onClick = {
                        onNavigate(it.route)
                    },
                )
            }
        },
    ) {
        content()
    }
}
