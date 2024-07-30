package com.apps.adrcotfas.goodtime.settings

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreference
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreferenceExtension
import com.apps.adrcotfas.goodtime.data.settings.prettyName
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel.Companion.firstDayOfWeekOptions
import com.apps.adrcotfas.goodtime.ui.common.RadioGroupDialog
import com.apps.adrcotfas.goodtime.ui.common.TimePicker
import com.apps.adrcotfas.goodtime.ui.common.RowWithCheckbox
import com.apps.adrcotfas.goodtime.ui.common.RowWithText
import com.apps.adrcotfas.goodtime.ui.common.SectionTitle
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle("Productivity Reminder")
            val reminderSettings = settings.productivityReminderSettings
            ProductivityReminderSection(
                firstDayOfWeek = DayOfWeek(settings.firstDayOfWeek),
                selectedDays = reminderSettings.days.map { DayOfWeek(it) }.toSet(),
                reminderSecondOfDay = reminderSettings.secondOfDay,
                onSelectDay = viewModel::onToggleProductivityReminderDay,
                onReminderTimeClick = { viewModel.setShowTimePicker(true) }
            )
            SectionTitle(
                text = "General",
                paddingValues = PaddingValues(
                    top = 16.dp,
                    bottom = 4.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            )
            RowWithText(
                title = "Workday start",
                subtitle = "Used for displaying the stats accordingly",
                value = secondsOfDayToTimerFormat(
                    settings.workdayStart,
                    DateFormat.is24HourFormat(context)
                ), onClick = {
                    viewModel.setShowWorkdayStartPicker(true)
                })
            RowWithText(
                title = "Start of the week",
                value = DayOfWeek.of(settings.firstDayOfWeek)
                    .getDisplayName(TextStyle.FULL, locale),
                onClick = {
                    viewModel.setShowFirstDayOfWeekPicker(true)
                }
            )
            SectionTitle(
                text = "User Interface",
                paddingValues = PaddingValues(
                    top = 24.dp,
                    bottom = 4.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            )
            RowWithCheckbox(
                title = "Use Dynamic Color",
                checked = settings.uiSettings.useDynamicColor
            ) {
                viewModel.setUseDynamicColor(it)
            }
            RowWithText(
                title = "Dark mode preference",
                value = settings.uiSettings.darkModePreference.prettyName,
                onClick = {
                    viewModel.setShowThemePicker(true)
                }
            )
            RowWithCheckbox(
                title = "Fullscreen mode",
                checked = settings.uiSettings.fullscreenMode
            ) {
                viewModel.setFullscreenMode(it)
            }
            RowWithCheckbox(
                title = "Keep the screen on",
                checked = settings.uiSettings.keepScreenOn
            ) {
                viewModel.setKeepScreenOn(it)
            }
            RowWithCheckbox(
                title = "Screensaver mode",
                checked = settings.uiSettings.screensaverMode
            ) {
                viewModel.setScreensaverMode(it)
            }
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
                radioOptions = DarkModePreferenceExtension.prettyNames(),
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toSecondOfDay(): Int {
    return LocalTime(hour = hour, minute = minute).toSecondOfDay()
}