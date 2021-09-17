package com.apps.adrcotfas.goodtime.ui.common

import android.content.Context
import android.text.format.DateFormat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.*

class TimePickerDialogBuilder(context: Context) {

    private val is24HourFormat = DateFormat.is24HourFormat(context)

    fun buildDialog(localTime: LocalTime): MaterialTimePicker {
        return MaterialTimePicker.Builder()
                .setHour(localTime.hour)
                .setMinute(localTime.minute)
                .setTimeFormat(if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                .build()
    }
}
