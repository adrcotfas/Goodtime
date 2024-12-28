package com.apps.adrcotfas.goodtime.settings.user_interface

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.notifications.NotificationArchManager
import com.apps.adrcotfas.goodtime.common.findActivity
import com.apps.adrcotfas.goodtime.common.getAppLanguage
import com.apps.adrcotfas.goodtime.common.prettyName
import com.apps.adrcotfas.goodtime.common.prettyNames
import com.apps.adrcotfas.goodtime.data.settings.ThemePreference
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel.Companion.firstDayOfWeekOptions
import com.apps.adrcotfas.goodtime.settings.toSecondOfDay
import com.apps.adrcotfas.goodtime.ui.common.CheckboxListItem
import com.apps.adrcotfas.goodtime.ui.common.CompactPreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.DropdownMenuListItem
import com.apps.adrcotfas.goodtime.ui.common.BetterListItem
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import com.apps.adrcotfas.goodtime.ui.common.TimePicker
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.apps.adrcotfas.goodtime.utils.secondsOfDayToTimerFormat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNavigateBack: () -> Boolean,
    showTopBar: Boolean,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locale = context.resources.configuration.locales[0]

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val notificationManager = koinInject<NotificationArchManager>()
    var isNotificationPolicyAccessGranted by remember { mutableStateOf(notificationManager.isNotificationPolicyAccessGranted()) }
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                isNotificationPolicyAccessGranted =
                    notificationManager.isNotificationPolicyAccessGranted()
                if (isNotificationPolicyAccessGranted) {
                    viewModel.setDndDuringWork(true)
                }
            }

            else -> {
                // do nothing
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                isVisible = showTopBar,
                title = "General settings",
                onNavigateBack = { onNavigateBack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val activity = context.findActivity()
                BetterListItem(
                    title = "Language",
                    trailing = context.getAppLanguage(),
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                        intent.data = Uri.fromParts("package", activity?.packageName, null)
                        activity?.startActivity(intent)
                    }
                )
            }
            BetterListItem(
                title = "Workday start",
                trailing = secondsOfDayToTimerFormat(
                    uiState.settings.workdayStart,
                    DateFormat.is24HourFormat(context)
                ),
                onClick = {
                    viewModel.setShowWorkdayStartPicker(true)
                }
            )
            DropdownMenuListItem(
                title = "Start of the week",
                value = DayOfWeek.of(uiState.settings.firstDayOfWeek)
                    .getDisplayName(TextStyle.FULL, locale),
                dropdownMenuOptions = firstDayOfWeekOptions.map {
                    it.getDisplayName(
                        TextStyle.FULL,
                        locale
                    )
                },
                onDropdownMenuItemSelected = {
                    viewModel.setFirstDayOfWeek(firstDayOfWeekOptions[it].isoDayNumber)
                }
            )

            DropdownMenuListItem(
                title = "Theme",
                //TODO: use localized strings instead
                value = uiState.settings.uiSettings.themePreference.prettyName(),
                dropdownMenuOptions = prettyNames<ThemePreference>(),
                onDropdownMenuItemSelected = {
                    viewModel.setThemeOption(ThemePreference.entries[it])
                }
            )

            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "During work sessions")
            CheckboxListItem(
                title = "Fullscreen mode",
                checked = uiState.settings.uiSettings.fullscreenMode
            ) {
                viewModel.setFullscreenMode(it)
            }
            CheckboxListItem(
                title = "Keep the screen on",
                checked = uiState.settings.uiSettings.keepScreenOn
            ) {
                viewModel.setKeepScreenOn(it)
            }
            CheckboxListItem(
                title = "Screensaver mode",
                checked = uiState.settings.uiSettings.screensaverMode,
                enabled = uiState.settings.uiSettings.keepScreenOn
            ) {
                viewModel.setScreensaverMode(it)
            }
            CheckboxListItem(
                title = "Do not disturb mode",
                subtitle = if (isNotificationPolicyAccessGranted) null else "Click to grant permission",
                checked = uiState.settings.uiSettings.dndDuringWork
            ) {
                if (isNotificationPolicyAccessGranted) {
                    viewModel.setDndDuringWork(it)
                } else {
                    requestDndPolicyAccess(context.findActivity()!!)
                }
            }
        }
        if (uiState.showWorkdayStartPicker) {
            val workdayStart = LocalTime.fromSecondOfDay(uiState.settings.workdayStart)
            val timePickerState = rememberTimePickerState(
                initialHour = workdayStart.hour,
                initialMinute = workdayStart.minute,
                is24Hour = DateFormat.is24HourFormat(context)
            )
            TimePicker(
                onDismiss = { viewModel.setShowWorkdayStartPicker(false) },
                onConfirm = {
                    viewModel.setWorkDayStart(timePickerState.toSecondOfDay())
                    viewModel.setShowWorkdayStartPicker(false)
                },
                timePickerState = timePickerState
            )
        }
    }
}

private fun requestDndPolicyAccess(activity: ComponentActivity) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    activity.startActivity(intent)
}