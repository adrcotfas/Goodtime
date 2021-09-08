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

import android.app.Dialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import com.apps.adrcotfas.goodtime.R
import java.util.*

class TimePickerFragment : DialogFragment() {
    private var listener: OnTimeSetListener? = null
    private var calendar: Calendar? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (calendar == null) {
            calendar = Calendar.getInstance()
        }
        val hour = calendar!![Calendar.HOUR_OF_DAY]
        val minute = calendar!![Calendar.MINUTE]
        return TimePickerDialogFixedNougatSpinner(
            requireActivity(),
            R.style.DialogTheme,
            listener, hour, minute, DateFormat.is24HourFormat(context)
        )
    }

    companion object {
        fun newInstance(listener: OnTimeSetListener?, calendar: Calendar?): TimePickerFragment {
            val dialog = TimePickerFragment()
            dialog.listener = listener
            dialog.calendar = calendar
            return dialog
        }
    }
}