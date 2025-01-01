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
package com.apps.adrcotfas.goodtime.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.apps.adrcotfas.goodtime.common.askForDisableBatteryOptimization
import com.apps.adrcotfas.goodtime.common.findActivity
import com.apps.adrcotfas.goodtime.data.settings.NotificationPermissionState
import com.apps.adrcotfas.goodtime.settings.permissions.AskForPermissionCard
import com.apps.adrcotfas.goodtime.settings.permissions.getPermissionsState
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider

@Composable
fun ActionSection(notificationPermissionState: NotificationPermissionState, onNotificationPermissionGranted: (Boolean) -> Unit) {
    val context = LocalContext.current
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
            onNotificationPermissionGranted(granted)
        }

    val permissionsState = getPermissionsState()

    AnimatedVisibility(permissionsState.shouldAskForNotificationPermission || permissionsState.shouldAskForBatteryOptimizationRemoval) {
        Column {
            PreferenceGroupTitle(
                text = "Action required",
                paddingValues = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            )
            AnimatedVisibility(permissionsState.shouldAskForBatteryOptimizationRemoval) {
                AskForPermissionCard(
                    cta = "Allow",
                    description = "Allow this app to run in the background",
                    onClick = { context.askForDisableBatteryOptimization() },
                )
            }

            AnimatedVisibility(permissionsState.shouldAskForNotificationPermission) {
                AskForPermissionCard(
                    cta = "Allow",
                    description = "Allow notifications",
                    onClick = {
                        if (notificationPermissionState == NotificationPermissionState.DENIED && !shouldShowRequestPermissionRationale(
                                context.findActivity()!!,
                                Manifest.permission.POST_NOTIFICATIONS,
                            )
                        ) {
                            navigateToNotificationSettings(context)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            navigateToNotificationSettings(context)
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SubtleHorizontalDivider()
        }
    }
}

private fun navigateToNotificationSettings(context: Context) {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}
