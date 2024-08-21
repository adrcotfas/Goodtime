package com.apps.adrcotfas.goodtime.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.notifications.NotificationArchManager
import com.apps.adrcotfas.goodtime.bl.notifications.TorchManager
import com.apps.adrcotfas.goodtime.bl.notifications.VibrationPlayer
import com.apps.adrcotfas.goodtime.common.findActivity
import com.apps.adrcotfas.goodtime.common.getAppLanguage
import com.apps.adrcotfas.goodtime.common.prettyName
import com.apps.adrcotfas.goodtime.common.prettyNames
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreference
import com.apps.adrcotfas.goodtime.data.settings.FlashType
import com.apps.adrcotfas.goodtime.data.settings.SoundData
import com.apps.adrcotfas.goodtime.labels.add_edit.SliderRow
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel.Companion.firstDayOfWeekOptions
import com.apps.adrcotfas.goodtime.settings.notification_sounds.NotificationSoundPickerDialog
import com.apps.adrcotfas.goodtime.settings.notification_sounds.toSoundData
import com.apps.adrcotfas.goodtime.ui.common.CheckboxPreference
import com.apps.adrcotfas.goodtime.ui.common.CompactPreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import com.apps.adrcotfas.goodtime.ui.common.TextPreference
import com.apps.adrcotfas.goodtime.ui.common.TextPreferenceWithDropdownMenu
import com.apps.adrcotfas.goodtime.ui.common.TimePicker
import com.apps.adrcotfas.goodtime.utils.secondsOfDayToTimerFormat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {

    val notificationManager = koinInject<NotificationArchManager>()
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]

    var isNotificationPolicyAccessGranted by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val vibrationPlayer = koinInject<VibrationPlayer>()
    val torchManager = koinInject<TorchManager>()
    val isTorchAvailable = torchManager.isTorchAvailable()

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

    val workRingTone = toSoundData(settings.workFinishedSound)
    val breakRingTone = toSoundData(settings.breakFinishedSound)
    val candidateRingTone = uiState.notificationSoundCandidate?.let { toSoundData(it) }

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

            Box(contentAlignment = Alignment.CenterEnd) {
                TextPreferenceWithDropdownMenu(
                    title = "Start of the week",
                    value = DayOfWeek.of(settings.firstDayOfWeek)
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
            }

            CheckboxPreference(
                title = "Use Dynamic Color",
                checked = settings.uiSettings.useDynamicColor
            ) {
                viewModel.setUseDynamicColor(it)
            }
            TextPreferenceWithDropdownMenu(
                title = "Dark mode preference",
                //TODO: use localized strings instead
                value = settings.uiSettings.darkModePreference.prettyName(),
                dropdownMenuOptions = prettyNames<DarkModePreference>(),
                onDropdownMenuItemSelected = {
                    viewModel.setThemeOption(DarkModePreference.entries[it])
                }
            )

            SubtleHorizontalDivider()
            CompactPreferenceGroupTitle(text = "During work sessions")
            //TODO: implement this
            CheckboxPreference(
                title = "Fullscreen mode",
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
            //TODO: implement this
            CheckboxPreference(
                title = "Screensaver mode",
                checked = settings.uiSettings.screensaverMode,
                clickable = settings.uiSettings.keepScreenOn
            ) {
                viewModel.setScreensaverMode(it)
            }
            CheckboxPreference(
                title = "Do not disturb mode",
                subtitle = if (isNotificationPolicyAccessGranted) null else "Click to grant permission",
                checked = settings.uiSettings.dndDuringWork
            ) {
                if (isNotificationPolicyAccessGranted) {
                    viewModel.setDndDuringWork(it)
                } else {
                    requestDndPolicyAccess(context.findActivity()!!)
                }
            }

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
            //TODO: use a fullscreen intent to handle the "screen" flash?
            TextPreferenceWithDropdownMenu(
                title = "Flash type",
                subtitle = "A visual notification for silent environments",
                value = settings.flashType.prettyName(),
                //TODO: use localized names instead and be careful when removing the TORCH option
                dropdownMenuOptions = prettyNames<FlashType>().toMutableList().apply {
                    if (!isTorchAvailable) {
                        val index = FlashType.entries.indexOf(FlashType.TORCH)
                        remove(prettyNames<FlashType>()[index])
                    }
                },
                onDropdownMenuItemSelected = {
                    viewModel.setFlashType(FlashType.entries[it])
                }
            )
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
    }
}

@Composable
private fun notificationSoundName(it: SoundData) =
    if (it.isSilent) "Silent"
    else if (it.name.isEmpty()) "Default notification sound"
    else it.name

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toSecondOfDay(): Int {
    return LocalTime(hour = hour, minute = minute).toSecondOfDay()
}

internal fun requestDndPolicyAccess(activity: ComponentActivity) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    activity.startActivity(intent)
}