package com.apps.adrcotfas.goodtime.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

class TimeUtils {
    companion object {

        fun getDaysOfWeekShort(): ArrayList<String> {
            val daysOfWeekRaw = mutableListOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            )

            val result = ArrayList<String>(7)
            for (day in daysOfWeekRaw) {
                result.add(day.getDisplayName(TextStyle.NARROW, Locale.getDefault()))
            }
            return result
        }

        fun firstDayOfWeek(): DayOfWeek =
            WeekFields.of(Locale.getDefault()).firstDayOfWeek

        fun lastDayOfWeek(): DayOfWeek =
            DayOfWeek.of((firstDayOfWeek().value + 5) % DayOfWeek.values().size + 1)

        fun formatDateLong(date: LocalDate): String {
            return date.format(DateTimeFormatter.ofPattern("EEE', 'MMM d', ' yyyy"))
        }

        fun formatTime(time: LocalTime): String {
            return time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        }

        fun firstDayOfCurrentWeekMillis(): Long {
            return LocalDate.now()
                .atStartOfDay()
                .with(TemporalAdjusters.previousOrSame(firstDayOfWeek()))
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        }

        fun firstDayOfLastWeekMillis(): Long {
            return LocalDate.now()
                .minus(1, ChronoUnit.WEEKS)
                .atStartOfDay()
                .with(TemporalAdjusters.previousOrSame(firstDayOfWeek()))
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        }

        fun nowMillis(): Long {
            return LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    }
}

fun secondsOfDayToTimerFormat(seconds: Int, is24HourFormat: Boolean = true): String {
    val time = LocalTime.ofSecondOfDay(seconds.toLong())
    return time.format(
        DateTimeFormatter.ofPattern(
            if (is24HourFormat) "HH:mm"
            else "hh:mm a"
        )
    )
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

// The following two functions are required to convert from Zone date time to UTC date time and back
// MaterialDatePicker uses UTC time
fun Long.toUtcLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toLocalDateTime()

fun Long.toZoneLocalDateTime(): LocalDate =
    toUtcLocalDateTime().atZone(ZoneId.systemDefault()).toLocalDate()

fun Long.toLocalTime(): LocalTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime()
}

fun Long.toUtcMillis() = toLocalDateTime().atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC))
    .toInstant().toEpochMilli()

fun Pair<LocalDate, LocalTime>.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(this.first, this.second)
}

val LocalDateTime.millis
    get() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalTime.toFormattedTime(): String =
    this.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

fun LocalDate.toFormattedDate(): String =
    this.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

fun LocalDateTime.toFormattedDateTime(): String =
    this.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

fun startOfTodayMillis() =
    LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun startOfTomorrowMillis() =
    LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun firstDayOfWeek(): DayOfWeek =
    WeekFields.of(Locale.getDefault()).firstDayOfWeek

fun lastDayOfWeek(): DayOfWeek =
    DayOfWeek.of((firstDayOfWeek().value + 5) % DayOfWeek.values().size + 1)
