/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.math.roundToInt

object StringUtils {

    private val backUpFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
    private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E")
    private val suffixes: NavigableMap<Long, String> = TreeMap()

    /**
     * Shortens a value in minutes to a easier to read format of maximum 4 characters
     * Do not use this for anything else than the History chart before removing the extra left padding hack
     * @param value the value in minutes to be formatted
     * @return the formatted value
     */
    fun formatLong(value: Long): String {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatLong(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + formatLong(-value)
        if (value < 1000) return value.toString() //deal with easy case
        val e = suffixes.floorEntry(value)!!
        val divideBy = e.key
        val suffix = e.value
        val truncated = value / (divideBy / 10) //the number part of the output times 10
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }

    fun formatDateAndTime(millis: Long): String {
        return millis.toLocalDateTime().format(backUpFormatter)
    }

    @JvmStatic
    fun formatTimeForStatistics(millis: Long?): String {
        val date = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(
                millis!!
            ), ZoneId.systemDefault()
        )
        return date.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    }

    @JvmStatic
    fun formatDateForStatistics(millis: Long?): String {
        val date = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(
                millis!!
            ), ZoneId.systemDefault()
        )
        return "${date.format(DateTimeFormatter.ofPattern("EEEE"))} ${date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))}"
    }

    fun toPercentage(value: Float): String {
        return (100 * value).roundToInt().toString() + "%"
    }

    fun toDayOfWeek(value: Int): String {
        return LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.of(value))).format(dayOfWeekFormatter)
    }

    fun toHourOfDay(hour: Int, is24HourFormat: Boolean): String {
        val time = LocalTime.now().withHour(hour)
        return time.format(
            DateTimeFormatter.ofPattern(
                if (is24HourFormat) "HH"
                else "hh a"
            )
        ).replace(" ", "\n")
    }

    init {
        suffixes[1000L] = "k"
        suffixes[1000000L] = "M"
        suffixes[1000000000L] = "G"
        suffixes[1000000000000L] = "T"
        suffixes[1000000000000000L] = "P"
        suffixes[1000000000000000000L] = "E"
    }

    fun formatMinutes(minutes: Long): String {
        val days = minutes / 1440
        val hours = minutes / 60 % 24
        val remMin = minutes % 60
        val result: String = if (minutes != 0L) {
            ((if (days != 0L) "${days}d\n" else "")
                    + (if (hours != 0L) "" + hours.toString() + "h" else "")
                    + if (remMin != 0L) " " + remMin.toString() + "m" else "")
        } else {
            "0m"
        }
        return result
    }
}