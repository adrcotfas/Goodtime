package com.apps.adrcotfas.goodtime.utils

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.AmPmMarker
import kotlinx.datetime.format.char

fun secondsOfDayToTimerFormat(secondOfDay: Int, is24HourFormat: Boolean = true): String {
    val time = LocalTime.fromSecondOfDay(secondOfDay)
    return time.format(
        if (is24HourFormat) {
            LocalTime.Format {
                hour(); char(':'); minute()
            }
        } else {
            LocalTime.Format {
                amPmHour(); char(':'); minute(); char(' ');
                amPmMarker(
                    AmPmMarker.AM.toString(), AmPmMarker.PM.toString()
                )
            }
        }
    )
}
