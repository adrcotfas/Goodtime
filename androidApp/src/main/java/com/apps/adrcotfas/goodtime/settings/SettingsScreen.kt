package com.apps.adrcotfas.goodtime.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.apps.adrcotfas.goodtime.common.askForDisableBatteryOptimization
import com.apps.adrcotfas.goodtime.common.findActivity
import com.apps.adrcotfas.goodtime.common.getVersionName
import com.apps.adrcotfas.goodtime.data.settings.NotificationPermissionState
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.settings.permissions.AskForPermissionCard
import com.apps.adrcotfas.goodtime.settings.permissions.getPermissionsState
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.PreferenceWithIcon
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Bell
import compose.icons.evaicons.outline.ColorPalette
import compose.icons.evaicons.outline.Info
import compose.icons.evaicons.outline.Save
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    navController: NavController
) {
    val context = LocalContext.current

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.setNotificationPermissionGranted(granted)
        }
    val notificationPermissionState by viewModel.uiState.map { it.notificationPermissionState }
        .collectAsStateWithLifecycle(initialValue = NotificationPermissionState.NOT_ASKED)

    val permissionsState = getPermissionsState()

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(permissionsState.shouldAskForNotificationPermission || permissionsState.shouldAskForBatteryOptimizationRemoval) {
                Column {
                    PreferenceGroupTitle(
                        text = "Action required",
                        paddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                    AnimatedVisibility(permissionsState.shouldAskForBatteryOptimizationRemoval) {
                        AskForPermissionCard(
                            cta = "Allow",
                            description = "Allow this app to run in the background",
                            onClick = { context.askForDisableBatteryOptimization() }
                        )
                    }

                    AnimatedVisibility(permissionsState.shouldAskForNotificationPermission) {
                        AskForPermissionCard(
                            cta = "Allow",
                            description = "Allow notifications",
                            onClick = {
                                if (notificationPermissionState == NotificationPermissionState.DENIED && !shouldShowRequestPermissionRationale(
                                        context.findActivity()!!,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                ) {
                                    navigateToNotificationSettings(context)
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    navigateToNotificationSettings(context)
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SubtleHorizontalDivider()
                }
            }
            PreferenceWithIcon(
                title = "User interface",
                icon = { Icon(EvaIcons.Outline.ColorPalette, contentDescription = "Feedback") },
                onClick = {
                    navController.navigate(Destination.UserInterfaceSettings.route)
                })
            PreferenceWithIcon(
                title = "Notifications",
                icon = { Icon(EvaIcons.Outline.Bell, contentDescription = "Feedback") },
                onClick = {
                    navController.navigate(Destination.NotificationSettings.route)
                })
            PreferenceWithIcon(
                title = "Backup and restore",
                icon = { Icon(EvaIcons.Outline.Save, contentDescription = "Feedback") },
                onClick = {
                    navController.navigate(Destination.Backup.route)
                })

            PreferenceWithIcon(
                title = "About and feedback",
                subtitle = "Goodtime Productivity ${context.getVersionName()}",
                icon = { Icon(EvaIcons.Outline.Info, contentDescription = "Feedback") },
                onClick = {
                    navController.navigate(Destination.About.route)
                })
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

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toSecondOfDay(): Int {
    return LocalTime(hour = hour, minute = minute).toSecondOfDay()
}

internal fun requestDndPolicyAccess(activity: ComponentActivity) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    activity.startActivity(intent)
}