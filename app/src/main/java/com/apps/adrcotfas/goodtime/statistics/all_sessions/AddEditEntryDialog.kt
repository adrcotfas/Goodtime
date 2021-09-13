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
package com.apps.adrcotfas.goodtime.statistics.all_sessions

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import com.apps.adrcotfas.goodtime.statistics.SessionViewModel
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import androidx.lifecycle.ViewModelProvider
import android.content.res.ColorStateList
import android.view.View
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog
import com.apps.adrcotfas.goodtime.statistics.main.StatisticsActivity
import android.widget.Toast
import com.apps.adrcotfas.goodtime.util.TimePickerFragment
import com.apps.adrcotfas.goodtime.util.DatePickerFragment
import android.widget.TimePicker
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.DialogAddEntryBinding
import com.apps.adrcotfas.goodtime.util.StringUtils
import org.joda.time.DateTime
import java.util.*
import kotlin.math.min

class AddEditEntryDialog : BottomSheetDialogFragment(), OnDateSetListener, OnTimeSetListener,
    OnLabelSelectedListener {

    private lateinit var mViewModel: AddEditEntryDialogViewModel
    private lateinit var mSessionViewModel: SessionViewModel
    private lateinit var mLabelsViewModel: LabelsViewModel
    private var mSessionToEdit: Session? = null

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogAddEntryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_add_entry, null, false
        )
        val view = binding.root
        val viewModelProvider = ViewModelProvider(this)
        mViewModel = viewModelProvider.get(AddEditEntryDialogViewModel::class.java)
        mSessionViewModel = viewModelProvider.get(SessionViewModel::class.java)
        mLabelsViewModel = viewModelProvider.get(LabelsViewModel::class.java)
        mViewModel.duration.observe(viewLifecycleOwner, { d: Int ->
            val duration = d.toString()
            binding.duration.setText(duration)
            binding.duration.setSelection(duration.length)
        })
        mViewModel.date.observe(viewLifecycleOwner, { date: DateTime ->
            binding.editDate.text = StringUtils.formatDate(date.millis)
            binding.editTime.text = StringUtils.formatTime(date.millis)
        })
        mViewModel.label.observe(viewLifecycleOwner, { label: String? ->
            if (label != null && label != getString(R.string.label_unlabeled)) {
                binding.labelChip.text = label
                mLabelsViewModel.getColorOfLabel(label)
                    .observe(viewLifecycleOwner, { color: Int? ->
                        binding.labelChip.chipBackgroundColor = ColorStateList.valueOf(
                            ThemeHelper.getColor(
                                requireContext(), color!!
                            )
                        )
                    })
                binding.labelDrawable.setImageDrawable(resources.getDrawable(R.drawable.ic_label))
            } else {
                binding.labelChip.text = resources.getString(R.string.label_add)
                binding.labelChip.chipBackgroundColor = ColorStateList.valueOf(
                    ThemeHelper.getColor(
                        requireContext(), ThemeHelper.COLOR_INDEX_UNLABELED
                    )
                )
                binding.labelDrawable.setImageDrawable(resources.getDrawable(R.drawable.ic_label_off))
            }
        })
        binding.editDate.setOnClickListener { onDateViewClick() }
        binding.editTime.setOnClickListener { onTimeViewClick() }
        binding.labelChip.setOnClickListener {
            // open another dialog to select the chip
            val fragmentManager = requireActivity().supportFragmentManager
            SelectLabelDialog.newInstance(this, mViewModel.label.value?: "", false)
                .show(fragmentManager, StatisticsActivity.DIALOG_SELECT_LABEL_TAG)
        }
        binding.save.setOnClickListener {
            if (binding.duration.text.toString().isEmpty()) {
                Toast.makeText(
                    activity,
                    getString(R.string.session_enter_valid_duration),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val duration = min(binding.duration.text.toString().toInt(), 240)
                val label = mViewModel.label.value
                val sessionToAdd = Session(
                    0, mViewModel.date.value!!
                        .millis, duration, label
                )
                if (mViewModel.sessionToEditId != AddEditEntryDialogViewModel.INVALID_SESSION_TO_EDIT_ID.toLong()) {
                    mSessionViewModel.editSession(
                        mViewModel.sessionToEditId,
                        sessionToAdd.timestamp,
                        sessionToAdd.duration.toLong(),
                        sessionToAdd.label
                    )
                } else {
                    mSessionViewModel.addSession(sessionToAdd)
                }
                dismiss()
            }
        }

        // this is for the edit dialog
        if (mSessionToEdit != null) {
            mViewModel.date.value = DateTime(mSessionToEdit!!.timestamp)
            mViewModel.duration.value = mSessionToEdit!!.duration
            mViewModel.label.value = mSessionToEdit!!.label
            mViewModel.sessionToEditId = mSessionToEdit!!.id
            binding.header.text = getString(R.string.session_edit_session)
        }
        return view
    }

    private val calendar: Calendar
        get() {
            val calendar = Calendar.getInstance()
            val date = mViewModel.date.value
            if (date != null) {
                calendar.time = date.toDate()
            }
            return calendar
        }

    private fun onTimeViewClick() {
        val d = TimePickerFragment.newInstance(this@AddEditEntryDialog, calendar)
        val fragmentManager = parentFragmentManager
        d.show(fragmentManager, StatisticsActivity.DIALOG_TIME_PICKER_TAG)
    }

    private fun onDateViewClick() {
        val d: DialogFragment = DatePickerFragment.newInstance(this@AddEditEntryDialog, calendar)
        val fragmentManager = parentFragmentManager
        d.show(fragmentManager, StatisticsActivity.DIALOG_DATE_PICKER_TAG)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        mViewModel.date.value =
            if (mViewModel.date.value == null) DateTime() else mViewModel.date.value!!
                .withHourOfDay(hourOfDay)
                .withMinuteOfHour(minute)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        mViewModel.date.value =
            if (mViewModel.date.value == null) DateTime() else mViewModel.date.value!!
                .withYear(year)
                .withMonthOfYear(monthOfYear + 1)
                .withDayOfMonth(dayOfMonth)
    }

    override fun onLabelSelected(label: Label) {
        if (label.title != "unlabeled") {
            mViewModel.label.setValue(label.title)
        } else {
            mViewModel.label.setValue(null)
        }
    }

    companion object {
        /**
         * Creates a new instance from an existing session. To be used when editing a session.
         * @param session the session
         * @return the new instance initialized with the existing session's data
         */
        @JvmStatic
        fun newInstance(session: Session?): AddEditEntryDialog {
            val dialog = AddEditEntryDialog()
            dialog.mSessionToEdit = session
            return dialog
        }
    }
}