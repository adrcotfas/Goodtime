package com.apps.adrcotfas.goodtime.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.apps.adrcotfas.goodtime.common.findActivity
import com.apps.adrcotfas.goodtime.common.getAppLanguage
import com.apps.adrcotfas.goodtime.common.prettyName
import com.apps.adrcotfas.goodtime.common.prettyNames
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreference
import com.apps.adrcotfas.goodtime.data.settings.FlashType
import com.apps.adrcotfas.goodtime.data.settings.VibrationStrength
import com.apps.adrcotfas.goodtime.labels.add_edit.SliderRow
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel.Companion.firstDayOfWeekOptions
import com.apps.adrcotfas.goodtime.ui.common.CompactPreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.RadioGroupDialog
import com.apps.adrcotfas.goodtime.ui.common.CheckboxPreference
import com.apps.adrcotfas.goodtime.ui.common.SwitchPreference
import com.apps.adrcotfas.goodtime.ui.common.TextPreference
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import com.apps.adrcotfas.goodtime.ui.common.TimePicker
import com.apps.adrcotfas.goodtime.utils.secondsOfDayToTimerFormat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]

    Scaffold(
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            PreferenceGroupTitle("Productivity Reminder")
            val reminderSettings = settings.productivityReminderSettings
            ProductivityReminderSection(
                firstDayOfWeek = DayOfWeek(settings.firstDayOfWeek),
                selectedDays = reminderSettings.days.map { DayOfWeek(it) }.toSet(),
                reminderSecondOfDay = reminderSettings.secondOfDay,
                onSelectDay = viewModel::onToggleProductivityReminderDay,
                onReminderTimeClick = { viewModel.setShowTimePicker(true) }
            )

            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "General")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val activity = context.findActivity()
                TextPreference(title = "Language", value = context.getAppLanguage()) {
                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                    intent.data = Uri.fromParts("package", activity?.packageName, null)
                    activity?.startActivity(intent)
                }
            }
            TextPreference(
                title = "Workday start",
                subtitle = "Used for displaying the stats accordingly",
                value = secondsOfDayToTimerFormat(
                    settings.workdayStart,
                    DateFormat.is24HourFormat(context)
                ), onClick = {
                    viewModel.setShowWorkdayStartPicker(true)
                })
            TextPreference(
                title = "Start of the week",
                value = DayOfWeek.of(settings.firstDayOfWeek)
                    .getDisplayName(TextStyle.FULL, locale),
                onClick = {
                    viewModel.setShowFirstDayOfWeekPicker(true)
                }
            )

            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "User Interface")
            CheckboxPreference(
                title = "Use Dynamic Color",
                checked = settings.uiSettings.useDynamicColor
            ) {
                viewModel.setUseDynamicColor(it)
            }
            TextPreference(
                title = "Dark mode preference",
                value = settings.uiSettings.darkModePreference.prettyName(),
                onClick = {
                    viewModel.setShowThemePicker(true)
                }
            )

            CheckboxPreference(
                title = "Fullscreen mode",
                subtitle = "Immersive mode while working",
                checked = settings.uiSettings.fullscreenMode
            ) {
                viewModel.setFullscreenMode(it)
            }
            CheckboxPreference(
                title = "Keep the screen on",
                checked = settings.uiSettings.keepScreenOn
            ) {
                viewModel.setKeepScreenOn(it)
            }
            CheckboxPreference(
                title = "Screensaver mode",
                subtitle = "Moves the timer while ethe screen is on",
                checked = settings.uiSettings.screensaverMode,
                clickable = settings.uiSettings.keepScreenOn
            ) {
                viewModel.setScreensaverMode(it)
            }

            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "Notifications")
            SwitchPreference(
                title = "Notification sound",
                checked = settings.notificationSoundEnabled
            ) {
                viewModel.setNotificationSoundEnabled(it)
            }

            AnimatedVisibility(settings.notificationSoundEnabled) {
                Column {
                    TextPreference(
                        title = "Work finished sound",
                        subtitle = "Default", //TODO: get the name of the sound
                        value = ""
                    ) {
                        //TODO: open sound picker
                    }
                    TextPreference(
                        title = "Break finished sound",
                        subtitle = "Default", //TODO: get the name of the sound
                        value = ""
                    ) {
                        //TODO: open sound picker
                    }
                }
            }
            SliderRow(
                title = "Vibration strength",
                value = settings.vibrationStrength.ordinal,
                min = 0,
                max = VibrationStrength.entries.lastIndex,
                showSteps = true,
                onValueChange = { viewModel.setVibrationStrength(VibrationStrength.entries[it]) },
                onClick = { viewModel.setShowVibrationStrengthPicker(true) },
                valueNames = prettyNames<VibrationStrength>()
            )
            TextPreference(
                title = "Flash type",
                subtitle = "A visual notification for silent environments",
                value = settings.flashType.prettyName()
            ) {
                viewModel.setShowFlashTypePicker(true)
            }
            CheckboxPreference(
                title = "Insistent notification",
                subtitle = "Repeat the notification until it's cancelled",
                checked = settings.insistentNotification
            ) {
                viewModel.setInsistentNotification(it)
            }
            CheckboxPreference(
                title = "Auto start work",
                subtitle = "Start the work session after a break without user interaction",
                checked = settings.autoStartWork
            ) {
                viewModel.setAutoStartWork(it)
            }
            CheckboxPreference(
                title = "Auto start break",
                subtitle = "Start the break session after a work session without user interaction",
                checked = settings.autoStartBreak
            ) {
                viewModel.setAutoStartBreak(it)
            }
            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "During work sessions")
            CheckboxPreference(
                title = "Do not disturb mode",
                checked = settings.dndDuringWork
            ) {
                viewModel.setDndDuringWork(it)
            }

            //TODO: add back-up section
            //TODO: add about section
        }
        if (uiState.showTimePicker) {
            val reminderTime =
                LocalTime.fromSecondOfDay(settings.productivityReminderSettings.secondOfDay)
            val timePickerState = rememberTimePickerState(
                initialHour = reminderTime.hour,
                initialMinute = reminderTime.minute,
                is24Hour = DateFormat.is24HourFormat(context)
            )
            TimePicker(
                onDismiss = { viewModel.setShowTimePicker(false) },
                onConfirm = {
                    viewModel.setReminderTime(timePickerState.toSecondOfDay())
                    viewModel.setShowTimePicker(false)
                },
                timePickerState = timePickerState
            )
        }
        if (uiState.showThemePicker) {
            RadioGroupDialog(title = "Dark mode preference",
                initialIndex = settings.uiSettings.darkModePreference.ordinal,
                //TODO: use localized strings instead
                radioOptions = prettyNames<DarkModePreference>(),
                onItemSelected = { viewModel.setThemeOption(DarkModePreference.entries[it]) },
                onDismiss = { viewModel.setShowThemePicker(false) }
            )
        }
        if (uiState.showFirstDayOfWeekPicker) {
            RadioGroupDialog(title = "First day of the week",
                initialIndex = firstDayOfWeekOptions.indexOf(DayOfWeek.of(settings.firstDayOfWeek)),
                radioOptions = firstDayOfWeekOptions.map {
                    it.getDisplayName(
                        TextStyle.FULL,
                        locale
                    )
                },
                onItemSelected = { viewModel.setFirstDayOfWeek(firstDayOfWeekOptions[it].isoDayNumber) },
                onDismiss = { viewModel.setShowFirstDayOfWeekPicker(false) }
            )
        }
        if (uiState.showWorkdayStartPicker) {
            val workdayStart = LocalTime.fromSecondOfDay(settings.workdayStart)
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

        if (uiState.showFlashTypePicker) {
            RadioGroupDialog(
                title = "Flash type",
                initialIndex = settings.flashType.ordinal,
                //TODO: use a localized name instead
                radioOptions = prettyNames<FlashType>(),
                onItemSelected = { viewModel.setFlashType(FlashType.entries[it]) },
                onDismiss = { viewModel.setShowFlashTypePicker(false) })
        }
        if (uiState.showVibrationStrengthPicker) {
            RadioGroupDialog(
                title = "Vibration strength",
                initialIndex = settings.vibrationStrength.ordinal,
                //TODO: use a localized name instead
                radioOptions = prettyNames<VibrationStrength>(),
                onItemSelected = { viewModel.setVibrationStrength(VibrationStrength.entries[it]) },
                onDismiss = { viewModel.setShowVibrationStrengthPicker(false) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toSecondOfDay(): Int {
    return LocalTime(hour = hour, minute = minute).toSecondOfDay()
}
