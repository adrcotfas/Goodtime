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

    val labelName: String = Label.DEFAULT_LABEL_NAME,
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData()
)

@Serializable
data class ProductivityReminderSettings(
    val productivityReminderEnabled: Boolean = false,
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
    //TODO: consider the following
    // - timer font and size
)

@Serializable
enum class DarkModePreference {
    SYSTEM,
    LIGHT,
    DARK
}

val DarkModePreference.prettyName: String
    get() = DarkModePreferenceExtension.prettyNames()[this.ordinal]

object DarkModePreferenceExtension {
    fun prettyNames(): List<String> {
        return DarkModePreference.entries.map {
            it.name.lowercase()
                .replaceFirstChar { firstChar -> firstChar.uppercase() }
        }
    }
}

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
