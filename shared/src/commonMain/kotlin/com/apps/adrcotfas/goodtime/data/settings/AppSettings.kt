package com.apps.adrcotfas.goodtime.data.settings

import com.apps.adrcotfas.goodtime.data.model.Label
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.Serializable

data class AppSettings(
    private val version: Int = 1,
    val productivityReminderSettings: ProductivityReminderSettings = ProductivityReminderSettings(),
    val uiSettings: UiSettings = UiSettings(),

    val workdayStart: Int = LocalTime(0, 0).toSecondOfDay(),
    val firstDayOfWeek: Int = DayOfWeek.MONDAY.isoDayNumber,

    /** The name/URI of the sound file or empty for default*/
    val workFinishedSound: String = "",
    /** The name/URI of the sound file or empty for default*/
    val breakFinishedSound: String = "",
    val userSounds: Set<SoundData> = emptySet(),
    val vibrationStrength: Int = 3,
    val flashType: FlashType = FlashType.OFF,
    val insistentNotification: Boolean = false,
    /** only valid with insistentNotification off **/
    val autoStartWork: Boolean = false,
    /** only valid with insistentNotification off and for countdown timers **/
    val autoStartBreak: Boolean = false,

    val labelName: String = Label.DEFAULT_LABEL_NAME,
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData()
)

@Serializable
data class ProductivityReminderSettings(
    /** A list representing the days when the reminder was enabled, Monday is 1 and Sunday is 7. */
    val days: List<Int> = emptyList(),
    /** The time of the reminder represented in seconds of the day */
    val secondOfDay: Int = LocalTime(9, 0).toSecondOfDay(),
)

@Serializable
data class UiSettings(
    val useDynamicColor: Boolean = false,
    val darkModePreference: DarkModePreference = DarkModePreference.DARK,
    val fullscreenMode: Boolean = false,
    val keepScreenOn: Boolean = true,
    val screensaverMode: Boolean = false,
    val dndDuringWork: Boolean = false,
    //TODO: consider the following
    // - timer font and size
)

@Serializable
enum class DarkModePreference {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
enum class FlashType {
    OFF,
    SCREEN,
    TORCH
}

@Serializable
data class SoundData(
    val name: String = "",
    val uriString: String = "",
    val isSilent: Boolean = false
)