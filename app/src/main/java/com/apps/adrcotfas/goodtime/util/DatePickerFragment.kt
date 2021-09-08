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

import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import com.apps.adrcotfas.goodtime.R
import org.joda.time.LocalDate
import java.util.*

class DatePickerFragment : DialogFragment() {

    private var listener: OnDateSetListener? = null
    private var calendar: Calendar? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (calendar == null) {
            calendar = Calendar.getInstance()
        }
        val year = calendar!![Calendar.YEAR]
        val month = calendar!![Calendar.MONTH]
        val day = calendar!![Calendar.DAY_OF_MONTH]
        val d = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            listener, year, month, day
        )
        d.datePicker.maxDate = LocalDate().toDateTimeAtStartOfDay().millis
        return d
    }

    companion object {
        fun newInstance(listener: OnDateSetListener?, calendar: Calendar?): DatePickerFragment {
            val dialog = DatePickerFragment()
            dialog.listener = listener
            dialog.calendar = calendar
            return dialog
        }
    }
}