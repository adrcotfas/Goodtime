package com.apps.adrcotfas.goodtime.settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.common.getVersionName
import com.apps.adrcotfas.goodtime.common.isPortrait
import com.apps.adrcotfas.goodtime.data.settings.NotificationPermissionState
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.settings.about.AboutScreen
import com.apps.adrcotfas.goodtime.settings.about.LicensesScreen
import com.apps.adrcotfas.goodtime.settings.backup.BackupScreen
import com.apps.adrcotfas.goodtime.settings.notifications.NotificationsScreen
import com.apps.adrcotfas.goodtime.settings.user_interface.GeneralSettingsScreen
import com.apps.adrcotfas.goodtime.settings.user_interface.TimerStyleScreen
import com.apps.adrcotfas.goodtime.ui.common.PreferenceWithIcon
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.apps.adrcotfas.goodtime.ui.common.isContent
import com.apps.adrcotfas.goodtime.ui.common.navigateToDetail
import com.apps.adrcotfas.goodtime.ui.common.navigateToExtra
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Bell
import compose.icons.evaicons.outline.ColorPalette
import compose.icons.evaicons.outline.Info
import compose.icons.evaicons.outline.Save
import compose.icons.evaicons.outline.Settings
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    val onNavigateBack = { navigator.navigateBack() }
    BackHandler(navigator.canNavigateBack()) { onNavigateBack() }

    val notificationPermissionState by viewModel.uiState.map { it.settings.notificationPermissionState }
        .collectAsStateWithLifecycle(initialValue = NotificationPermissionState.NOT_ASKED)

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.isPortrait

    var isFirstOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isPortrait && !isFirstOpen) {
            isFirstOpen = true
            navigator.navigateToDetail(Destination.GeneralSettings.route)
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Scaffold(
                    topBar = {
                        TopBar(title = "Settings")
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding())
                            .verticalScroll(rememberScrollState())
                    ) {
                        ActionSection(
                            notificationPermissionState = notificationPermissionState,
                            onNotificationPermissionGranted = { granted ->
                                viewModel.setNotificationPermissionGranted(granted)
                            }
                        )
                        PreferenceWithIcon(
                            title = "General settings",
                            icon = {
                                Icon(
                                    EvaIcons.Outline.Settings,
                                    contentDescription = "General settings"
                                )
                            },
                            onClick = {
                                navigator.navigateToDetail(Destination.GeneralSettings.route)
                            },
                            isSelected = navigator.isContent(Destination.GeneralSettings.route)
                        )
                        PreferenceWithIcon(
                            title = "Timer style",
                            icon = {
                                Icon(
                                    EvaIcons.Outline.ColorPalette,
                                    contentDescription = "Timer style"
                                )
                            },
                            onClick = {
                                navigator.navigateToDetail(Destination.TimerStyle.route)
                            },
                            isSelected = navigator.isContent(Destination.TimerStyle.route)
                        )
                        PreferenceWithIcon(
                            title = "Notifications",
                            icon = {
                                Icon(
                                    EvaIcons.Outline.Bell,
                                    contentDescription = "Notifications"
                                )
                            },
                            onClick = {
                                navigator.navigateToDetail(Destination.NotificationSettings.route)
                            },
                            isSelected = navigator.isContent(Destination.NotificationSettings.route)
                        )
                        PreferenceWithIcon(
                            title = "Backup and restore",
                            icon = {
                                Icon(
                                    EvaIcons.Outline.Save,
                                    contentDescription = "Backup and restore"
                                )
                            },
                            onClick = {
                                navigator.navigateToDetail(Destination.Backup.route)
                            },
                            isSelected = navigator.isContent(Destination.Backup.route)
                        )

                        PreferenceWithIcon(
                            title = "About and feedback",
                            subtitle = "Goodtime Productivity ${context.getVersionName()}",
                            icon = {
                                Icon(
                                    EvaIcons.Outline.Info,
                                    contentDescription = "About and feedback"
                                )
                            },
                            onClick = {
                                navigator.navigateToDetail(Destination.About.route)
                            },
                            isSelected = navigator.isContent(Destination.About.route)
                        )
                    }

                }
            }
        },
        detailPane = {
            AnimatedPane {
                val content = navigator.currentDestination?.content ?: ""
                when (content) {
                    Destination.GeneralSettings.route -> GeneralSettingsScreen(
                        showTopBar = isPortrait,
                        onNavigateBack = onNavigateBack
                    )

                    Destination.TimerStyle.route -> TimerStyleScreen(
                        showTopBar = isPortrait,
                        onNavigateBack = onNavigateBack
                    )

                    Destination.NotificationSettings.route -> NotificationsScreen(
                        showTopBar = isPortrait,
                        onNavigateBack = onNavigateBack
                    )

                    Destination.Backup.route -> BackupScreen(
                        showTopBar = isPortrait,
                        onNavigateBack = onNavigateBack
                    )

                    Destination.About.route, Destination.Licenses.route -> AboutScreen(
                        onNavigateToLicenses = {
                            navigator.navigateToExtra(Destination.Licenses.route)
                        },
                        showTopBar = isPortrait || navigator.isContent(Destination.Licenses.route),
                        onNavigateBack = onNavigateBack,
                        isLicensesSelected = navigator.isContent(Destination.Licenses.route)
                    )
                }
            }
        },
        extraPane = {
            AnimatedPane {
                //TODO: highlight "Open source licenses" while open
                val content = navigator.currentDestination?.content ?: ""
                when (content) {
                    Destination.Licenses.route -> LicensesScreen(
                        showTopBar = isPortrait,
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toSecondOfDay(): Int {
    return LocalTime(hour = hour, minute = minute).toSecondOfDay()
}