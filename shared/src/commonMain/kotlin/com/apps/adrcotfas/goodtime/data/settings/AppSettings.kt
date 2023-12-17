package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.datetime.LocalTime
import kotlinx.serialization.*

data class AppSettings(
    private val version: Int = 1,
    val productivityReminderSettings: ProductivityReminderSettings,
    val uiSettings: UiSettings,

    val defaultTimerProfile: DefaultTimerProfile,
    val notificationSoundEnabled: Boolean,
    /** The name/URI of the sound file or empty for default*/
    val workFinishedSound: String,
    /** The name/URI of the sound file or empty for default*/
    val breakFinishedSound: String,
    val vibrationStrength: VibrationStrength,
    val flashType: FlashType,
    val insistentNotification: Boolean,
    /** only valid with insistentNotification off **/
    val autoStartWork: Boolean,
    /** only valid with insistentNotification off and for countdown timers **/
    val autoStartBreak: Boolean,
    val dndDuringWork: Boolean,
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
sealed class DefaultTimerProfile {
    @Serializable
    @SerialName("countdown")
    data class Countdown(
        /** Work duration in minutes; invalid for isCountdown false */
        val workDuration: Int = 25,
        /** Break duration in minutes */
        val breakDuration: Int = 5,
        /** Long break duration in minutes */
        val longBreakDuration: Int = 15,
        /** Number of sessions before long break or 0 to have this feature disabled */
        val sessionsBeforeLongBreak: Int = 4
    ) : DefaultTimerProfile()

    @Serializable
    @SerialName("flow")
    data class Flow(
        /** only valid with isCountdown false **/
        val workBreakRatio: Int = 3
    ): DefaultTimerProfile()
}

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
