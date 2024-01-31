package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

data class AppSettings(
    private val version: Int = 1,
    val productivityReminderSettings: ProductivityReminderSettings = ProductivityReminderSettings(),
    val uiSettings: UiSettings = UiSettings(),

    val notificationSoundEnabled: Boolean = true,
    /** The name/URI of the sound file or empty for default*/
    val workFinishedSound: String = "",
    /** The name/URI of the sound file or empty for default*/
    val breakFinishedSound: String = "",
    val vibrationStrength: VibrationStrength = VibrationStrength.MEDIUM,
    val flashType: FlashType = FlashType.OFF,
    val insistentNotification: Boolean = false,
    /** only valid with insistentNotification off **/
    val autoStartWork: Boolean = false,
    /** only valid with insistentNotification off and for countdown timers **/
    val autoStartBreak: Boolean = false,
    val dndDuringWork: Boolean = false,
    val persistedTimerData: PersistedTimerData = PersistedTimerData()
)

@Serializable
data class ProductivityReminderSettings(
    val productivityReminderEnabled: Boolean = false,
    /** A list representing the days when the reminder was enabled, Monday through Sunday. */
    val productivityReminderDays: List<Boolean> = List(7) { false },
    /** The time of the reminder represented in seconds of the day */
    val productivityReminderSecondOfDay: Int = LocalTime(9, 0).toSecondOfDay(),
)

@Serializable
data class UiSettings(
    val followSystemTheme: Boolean = false,
    val darkTheme: Boolean = true,
    val fullscreenMode: Boolean = false,
    val keepScreenOn: Boolean = true,
    val screensaverMode: Boolean = false
)

@Serializable
enum class VibrationStrength {
    OFF,
    LOW,
    MEDIUM,
    HIGH
}

@Serializable
enum class FlashType {
    OFF,
    SCREEN,
    CAMERA
}
