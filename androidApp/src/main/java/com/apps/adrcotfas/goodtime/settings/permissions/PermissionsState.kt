package com.apps.adrcotfas.goodtime.settings.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.apps.adrcotfas.goodtime.common.areNotificationsEnabled
import com.apps.adrcotfas.goodtime.common.isIgnoringBatteryOptimizations

data class PermissionsState(
    val shouldAskForNotificationPermission: Boolean,
    val shouldAskForBatteryOptimizationRemoval: Boolean
)

@Composable
fun getPermissionsState(): PermissionsState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    var shouldAskForNotificationPermission by remember { mutableStateOf(!context.areNotificationsEnabled()) }
    var shouldAskForBatteryOptimizationRemoval by remember { mutableStateOf(!context.isIgnoringBatteryOptimizations()) }

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                shouldAskForNotificationPermission = !context.areNotificationsEnabled()
                shouldAskForBatteryOptimizationRemoval = !context.isIgnoringBatteryOptimizations()
            }

            else -> {
                // do nothing
            }
        }
    }
    return PermissionsState(
        shouldAskForNotificationPermission = shouldAskForNotificationPermission,
        shouldAskForBatteryOptimizationRemoval = shouldAskForBatteryOptimizationRemoval
    )
}