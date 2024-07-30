package com.apps.adrcotfas.goodtime.settings

import android.text.format.DateFormat
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
import com.apps.adrcotfas.goodtime.ui.common.TimePicker
import com.apps.adrcotfas.goodtime.ui.common.RowWithCheckbox
import com.apps.adrcotfas.goodtime.ui.common.SectionTitle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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
                firstDayOfWeek = DayOfWeek(reminderSettings.firstDayOfWeek),
                selectedDays = reminderSettings.days.map { DayOfWeek(it) }.toSet(),
                reminderSecondOfDay = reminderSettings.secondOfDay,
                onSelectDay = viewModel::onToggleProductivityReminderDay,
                onReminderTimeClick = { viewModel.setShowTimePicker(true) }
            )

            RowWithCheckbox(
                title = "Use Dynamic Color",
                checked = settings.uiSettings.useDynamicColor
            ) {
                viewModel.setUseDynamicColor(it)
            }
            RowWithCheckbox(
                title = "Fullscreen mode",
                checked = settings.uiSettings.fullscreenMode
            ) {

            }
            RowWithCheckbox(
                title = "Keep the screen on",
                checked = settings.uiSettings.keepScreenOn
            ) {

            }
            RowWithCheckbox(
                title = "Screensaver mode",
                checked = settings.uiSettings.screensaverMode
            ) {

            }
        }
        if (uiState.showTimePicker) {
            val context = LocalContext.current
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toSecondOfDay(): Int {
    return LocalTime(hour = hour, minute = minute).toSecondOfDay()
}