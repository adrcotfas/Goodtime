/*
 * Copyright 2016-2020 Adrian Cotfas
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

import android.R
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Build
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.TimePicker
import com.apps.adrcotfas.goodtime.util.TimePickerDialogFixedNougatSpinner
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.reflect.Field

/**
 * Workaround for this bug: https://code.google.com/p/android/issues/detail?id=222208
 * In Android 7.0 Nougat, spinner mode for the TimePicker in TimePickerDialog is
 * incorrectly displayed as clock, even when the theme specifies otherwise, such as:
 *
 * <resources>
 * <style name="Theme.MyApp" parent="Theme.AppCompat.Light.NoActionBar">
<item name="android:timePickerStyle">@style/Widget.MyApp.TimePicker</item>
</style>
 *
 * <style name="Widget.MyApp.TimePicker" parent="android:Widget.Material.TimePicker">
<item name="android:timePickerMode">spinner</item>
</style>
</resources> *
 *
 * May also pass TimePickerDialog.THEME_HOLO_LIGHT as an argument to the constructor,
 * as this theme has the TimePickerMode set to spinner.
 */
class TimePickerDialogFixedNougatSpinner : TimePickerDialog {
    /**
     * Creates a new time picker dialog.
     *
     * @param context the parent context
     * @param listener the listener to call when the time is set
     * @param hourOfDay the initial hour
     * @param minute the initial minute
     * @param is24HourView whether this is a 24 hour view or AM/PM
     */
    constructor(
        context: Context,
        listener: OnTimeSetListener?,
        hourOfDay: Int,
        minute: Int,
        is24HourView: Boolean
    ) : super(context, listener, hourOfDay, minute, is24HourView) {
        fixSpinner(context, hourOfDay, minute, is24HourView)
    }

    /**
     * Creates a new time picker dialog with the specified theme.
     *
     * @param context the parent context
     * @param themeResId the resource ID of the theme to apply to this dialog
     * @param listener the listener to call when the time is set
     * @param hourOfDay the initial hour
     * @param minute the initial minute
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    constructor(
        context: Context,
        themeResId: Int,
        listener: OnTimeSetListener?,
        hourOfDay: Int,
        minute: Int,
        is24HourView: Boolean
    ) : super(context, themeResId, listener, hourOfDay, minute, is24HourView) {
        fixSpinner(context, hourOfDay, minute, is24HourView)
    }

    private fun fixSpinner(context: Context, hourOfDay: Int, minute: Int, is24HourView: Boolean) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) { // fixes the bug in API 24
            try {
                // Get the theme's android:timePickerMode
                val MODE_SPINNER = 1
                @SuppressLint("PrivateApi") val styleableClass =
                    Class.forName("com.android.internal.R\$styleable")
                val timePickerStyleableField = styleableClass.getField("TimePicker")
                val timePickerStyleable = timePickerStyleableField[null] as IntArray
                val a = context.obtainStyledAttributes(
                    null,
                    timePickerStyleable,
                    R.attr.timePickerStyle,
                    0
                )
                val timePickerModeStyleableField =
                    styleableClass.getField("TimePicker_timePickerMode")
                val timePickerModeStyleable = timePickerModeStyleableField.getInt(null)
                val mode = a.getInt(timePickerModeStyleable, MODE_SPINNER)
                a.recycle()
                if (mode == MODE_SPINNER) {
                    val timePicker = findField(
                        TimePickerDialog::class.java, TimePicker::class.java, "mTimePicker"
                    )!![this] as TimePicker
                    val delegateClass =
                        Class.forName("android.widget.TimePicker\$TimePickerDelegate")
                    val delegateField = findField(
                        TimePicker::class.java, delegateClass, "mDelegate"
                    )
                    var delegate = delegateField!![timePicker]
                    val spinnerDelegateClass: Class<*>
                    spinnerDelegateClass = Class.forName("android.widget.TimePickerSpinnerDelegate")
                    // In 7.0 Nougat for some reason the timePickerMode is ignored and the delegate is TimePickerClockDelegate
                    if (delegate.javaClass != spinnerDelegateClass) {
                        delegateField[timePicker] = null // throw out the TimePickerClockDelegate!
                        timePicker.removeAllViews() // remove the TimePickerClockDelegate views
                        val spinnerDelegateConstructor = spinnerDelegateClass.getConstructor(
                            TimePicker::class.java,
                            Context::class.java,
                            AttributeSet::class.java,
                            Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType
                        )
                        spinnerDelegateConstructor.isAccessible = true
                        // Instantiate a TimePickerSpinnerDelegate
                        delegate = spinnerDelegateConstructor.newInstance(
                            timePicker,
                            context,
                            null,
                            R.attr.timePickerStyle,
                            0
                        )
                        delegateField[timePicker] =
                            delegate // set the TimePicker.mDelegate to the spinner delegate
                        // Set up the TimePicker again, with the TimePickerSpinnerDelegate
                        timePicker.setIs24HourView(is24HourView)
                        timePicker.currentHour = hourOfDay
                        timePicker.currentMinute = minute
                        timePicker.setOnTimeChangedListener(this)
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        private fun findField(
            objectClass: Class<*>,
            fieldClass: Class<*>,
            expectedName: String
        ): Field? {
            try {
                val field = objectClass.getDeclaredField(expectedName)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
            } // ignore
            // search for it if it wasn't found under the expected ivar name
            for (searchField in objectClass.declaredFields) {
                if (searchField.type == fieldClass) {
                    searchField.isAccessible = true
                    return searchField
                }
            }
            return null
        }
    }
}