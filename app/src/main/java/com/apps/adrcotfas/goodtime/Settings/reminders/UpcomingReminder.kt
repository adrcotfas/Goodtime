package com.apps.adrcotfas.goodtime.Settings.reminders

import org.joda.time.LocalTime

class UpcomingReminder {
    companion object {
        fun calculateNextAlertInMillis(hourOfDay: Int, minute: Int, selectedWeekdays: List<Int?>): Long {
            val nowTime = LocalTime().toDateTimeToday()
            var nextReminderTime = LocalTime(hourOfDay, minute).toDateTimeToday()
            if (nowTime.isAfter(nextReminderTime)) {
                nextReminderTime = nextReminderTime.plusDays(1)
            }
            if (selectedWeekdays.isEmpty()) {
                return nextReminderTime.millis
            }
            while (!selectedWeekdays.contains(nextReminderTime.dayOfWeek)) {
                nextReminderTime = nextReminderTime.plusDays(1)
            }
            return nextReminderTime.millis
        }
    }
}