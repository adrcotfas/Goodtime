/*
 * Copyright 2016-2021 Adrian Cotfas
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
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import com.apps.adrcotfas.goodtime.statistics.SessionViewModel
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import android.content.res.ColorStateList
import android.view.View
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog
import com.apps.adrcotfas.goodtime.statistics.main.StatisticsActivity
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.DialogAddEntryBinding
import com.apps.adrcotfas.goodtime.ui.common.DatePickerDialogHelper
import com.apps.adrcotfas.goodtime.ui.common.TimePickerDialogBuilder
import com.apps.adrcotfas.goodtime.util.*
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Integer.min
import java.time.LocalTime
import java.util.*

@AndroidEntryPoint
class AddEditEntryDialog : BottomSheetDialogFragment(), OnLabelSelectedListener {

    private val viewModel: AddEditEntryDialogViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by viewModels()
    private val labelsViewModel: LabelsViewModel by viewModels()

    private lateinit var binding: DialogAddEntryBinding

    private lateinit var candidateSession: Session
    private var isEditDialog: Boolean = false

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_add_entry, null, false
        )

        if (savedInstanceState == null) {
            viewModel.session = candidateSession
        }

        if (isEditDialog) {
            binding.header.text = getString(R.string.session_edit_session)
        }

        setupDateAndTimePickers()
        setupDuration()
        setupLabel()
        setupSaveButton()

        return binding.root
    }

    private fun setupSaveButton() {
        binding.save.setOnClickListener {
            val durationValue = binding.duration.text.toString().toIntOrNull()
            viewModel.session.duration = min(durationValue ?: 0, 240)
            if (binding.duration.text.toString().isEmpty()) {
                Toast.makeText(
                    activity,
                    getString(R.string.session_enter_valid_duration),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                sessionViewModel.apply {
                    if (isEditDialog) {
                        editSession(viewModel.session)
                    } else {
                        addSession(viewModel.session)
                    }
                }
                dismiss()
            }
        }
    }

    private fun setupLabel() {
        val label = viewModel.session.label
        refreshLabel(label)

        binding.labelContainer.setOnClickListener {
            SelectLabelDialog.newInstance(this, viewModel.session.label ?: "", false)
                .showOnce(parentFragmentManager, StatisticsActivity.DIALOG_SELECT_LABEL_TAG)
        }
    }

    private fun refreshLabel(label: String?) {
        if (label != null && label != getString(R.string.label_unlabeled)) {
            binding.labelChip.text = label
            labelsViewModel.getColorOfLabel(label)
                .observe(viewLifecycleOwner) { color: Int? ->
                    binding.labelChip.chipBackgroundColor = ColorStateList.valueOf(
                        ThemeHelper.getColor(
                            requireContext(), color!!
                        )
                    )
                }
            binding.labelDrawable.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_label,
                    null
                )
            )
        } else {
            binding.labelChip.text = resources.getString(R.string.label_add)
            binding.labelChip.chipBackgroundColor = ColorStateList.valueOf(
                ThemeHelper.getColor(
                    requireContext(), ThemeHelper.COLOR_INDEX_UNLABELED
                )
            )
            binding.labelDrawable.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_label_off,
                    null
                )
            )
        }
    }

    private fun setupDuration() {
        if (isEditDialog) {
            val duration = viewModel.session.duration.toString()
            binding.duration.setText(duration)
            binding.duration.setSelection(duration.length)
        }
    }

    private fun setupDateAndTimePickers() {
        val timestamp = viewModel.session.timestamp
        val localTime = timestamp.toLocalTime()
        val localDate = timestamp.toLocalDate()

        binding.editTime.text = TimeUtils.formatTime(localTime)
        binding.editDate.text = TimeUtils.formatDateLong(localDate)

        binding.editTime.setOnClickListener {
            val dialog = TimePickerDialogBuilder(requireContext()).buildDialog(localTime)
            dialog.addOnPositiveButtonClickListener {
                val newLocalTime = LocalTime.of(dialog.hour, dialog.minute)
                viewModel.session.timestamp = Pair(localDate, newLocalTime).toLocalDateTime().millis
                binding.editTime.text = newLocalTime.toFormattedTime()
            }
            dialog.showOnce(parentFragmentManager, "MaterialTimePicker")
        }

        binding.editDate.setOnClickListener {
            val picker =
                DatePickerDialogHelper.buildDatePicker(
                    timestamp.toUtcMillis()
                )
            picker.addOnPositiveButtonClickListener {
                val newLocalDate =
                    it.toZoneLocalDateTime()
                viewModel.session.timestamp = Pair(newLocalDate, localTime).toLocalDateTime().millis
                binding.editDate.text =
                    TimeUtils.formatDateLong(newLocalDate) //TODO: extract as extension function
            }
            picker.show(parentFragmentManager, "MaterialDatePicker")
        }
    }

    override fun onLabelSelected(label: Label) {
        viewModel.session.label = if (label.title != "unlabeled") label.title else null
        refreshLabel(label.title)
    }

    companion object {
        /**
         * Creates a new instance from an existing session. To be used when editing a session.
         * @param session the session
         * @return the new instance initialized with the existing session's data
         */
        fun newInstance(session: Session?): AddEditEntryDialog {
            val dialog = AddEditEntryDialog()
            dialog.candidateSession = session ?: Session()
            dialog.isEditDialog = (session != null)
            return dialog
        }
    }
}