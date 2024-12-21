package com.apps.adrcotfas.goodtime.settings.notifications

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.notifications.TorchManager
import com.apps.adrcotfas.goodtime.bl.notifications.VibrationPlayer
import com.apps.adrcotfas.goodtime.data.settings.SoundData
import com.apps.adrcotfas.goodtime.labels.add_edit.SliderRow
import com.apps.adrcotfas.goodtime.settings.ProductivityReminderSection
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import com.apps.adrcotfas.goodtime.settings.toSecondOfDay
import com.apps.adrcotfas.goodtime.ui.common.CheckboxPreference
import com.apps.adrcotfas.goodtime.ui.common.CompactPreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import com.apps.adrcotfas.goodtime.ui.common.TextPreference
import com.apps.adrcotfas.goodtime.ui.common.TimePicker
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: SettingsViewModel = koinViewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onNavigateBack: () -> Boolean,
    showTopBar: Boolean,
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings

    val vibrationPlayer = koinInject<VibrationPlayer>()
    val torchManager = koinInject<TorchManager>()
    val isTorchAvailable = torchManager.isTorchAvailable()
    val workRingTone = toSoundData(settings.workFinishedSound)
    val breakRingTone = toSoundData(settings.breakFinishedSound)
    val candidateRingTone = uiState.notificationSoundCandidate?.let { toSoundData(it) }

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier.alpha(if (showTopBar) 1f else 0f),
                title = "Notifications",
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
            PreferenceGroupTitle(text = "Productivity Reminder")
            val reminderSettings = settings.productivityReminderSettings
            ProductivityReminderSection(
                firstDayOfWeek = DayOfWeek(settings.firstDayOfWeek),
                selectedDays = reminderSettings.days.map { DayOfWeek(it) }.toSet(),
                reminderSecondOfDay = reminderSettings.secondOfDay,
                onSelectDay = viewModel::onToggleProductivityReminderDay,
                onReminderTimeClick = { viewModel.setShowTimePicker(true) }
            )
            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "Notifications")
            TextPreference(
                title = "Work finished sound",
                subtitle = notificationSoundName(workRingTone),
                onClick = { viewModel.setShowSelectWorkSoundPicker(true) }
            )

            TextPreference(
                title = "Break finished sound",
                subtitle = notificationSoundName(breakRingTone),
                onClick = { viewModel.setShowSelectBreakSoundPicker(true) }
            )

            CheckboxPreference(
                title = "Override sound profile",
                subtitle = "The notification sound behaves like an alarm",
                checked = settings.overrideSoundProfile
            ) {
                viewModel.setOverrideSoundProfile(it)
            }

            var selectedStrength = settings.vibrationStrength
            SliderRow(
                title = "Vibration strength",
                value = settings.vibrationStrength,
                min = 0,
                max = 5,
                showSteps = true,
                onValueChange = {
                    selectedStrength = it
                    viewModel.setVibrationStrength(it)
                },
                onValueChangeFinished = { vibrationPlayer.start(selectedStrength) },
                showValue = false
            )
            if (isTorchAvailable) {
                CheckboxPreference(
                    title = "Torch",
                    subtitle = "A visual notification for silent environments",
                    checked = settings.enableTorch
                ) {
                    viewModel.setEnableTorch(it)
                }
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
        }

        if (uiState.showSelectWorkSoundPicker) {
            NotificationSoundPickerDialog(
                title = "Work finished sound",
                selectedItem = candidateRingTone ?: workRingTone,
                onSelected = {
                    viewModel.setNotificationSoundCandidate(Json.encodeToString(it))
                },
                onSave = { viewModel.setWorkFinishedSound(Json.encodeToString(it)) },
                onDismiss = { viewModel.setShowSelectWorkSoundPicker(false) }
            )
        }
        if (uiState.showSelectBreakSoundPicker) {
            NotificationSoundPickerDialog(
                title = "Break finished sound",
                selectedItem = candidateRingTone ?: breakRingTone,
                onSelected = {
                    viewModel.setNotificationSoundCandidate(Json.encodeToString(it))
                },
                onSave = { viewModel.setBreakFinishedSound(Json.encodeToString(it)) },
                onDismiss = { viewModel.setShowSelectBreakSoundPicker(false) }
            )
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
    }
}

@Composable
private fun notificationSoundName(it: SoundData) =
    if (it.isSilent) "Silent"
    else if (it.name.isEmpty()) "Default notification sound"
    else it.name
