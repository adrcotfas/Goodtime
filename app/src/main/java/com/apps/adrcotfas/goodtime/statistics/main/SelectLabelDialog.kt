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
package com.apps.adrcotfas.goodtime.statistics.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Profile
import com.apps.adrcotfas.goodtime.databinding.DialogSelectLabelBinding
import com.apps.adrcotfas.goodtime.labels.AddEditLabelActivity
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.settings.ProfilesViewModel
import com.apps.adrcotfas.goodtime.statistics.Utils
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import com.apps.adrcotfas.goodtime.util.UpgradeDialogHelper.Companion.launchUpgradeDialog
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class SelectLabelDialog : DialogFragment() {

    /**
     * The callback used to indicate the user is done selecting the title
     */
    interface OnLabelSelectedListener {
        /**
         * @param label     the label that was set
         */
        fun onLabelSelected(label: Label)
    }

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    private lateinit var mProfiles: List<Profile>
    private lateinit var mLabel: String
    private lateinit var mCallback: WeakReference<OnLabelSelectedListener>

    /**
     * The extended version of this dialog is used in the Statistics
     * where it also contains "all" as a label.
     */
    private var mIsExtendedVersion = false

    /**
     * The neutral button used to change the profile
     */
    private var showProfileSelection = false

    private var mAlertDialog: AlertDialog? = null

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogSelectLabelBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.dialog_select_label,
            null,
            false
        )
        binding.editLabels.setOnClickListener {
            if (preferenceHelper.isPro()) {
                val intent = Intent(activity, AddEditLabelActivity::class.java)
                startActivity(intent)
            } else {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
            }
            if (mAlertDialog != null) {
                mAlertDialog!!.dismiss()
            }
        }
        val viewModel: LabelsViewModel by viewModels()
        viewModel.labels.observe(this) { labels: List<Label> ->
            var i = 0
            if (mIsExtendedVersion) {
                val chip = Chip(requireContext())
                val total = Utils.getInstanceTotalLabel(requireContext())
                chip.text = total.title
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ThemeHelper.getColor(
                        requireContext(),
                        ThemeHelper.COLOR_INDEX_ALL_LABELS
                    )
                )
                chip.isCheckable = true
                chip.chipIcon =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_check_off, null)
                chip.checkedIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_check, null)
                chip.id = i++
                if (chip.text.toString() == mLabel) {
                    chip.isChecked = true
                }
                binding.labels.addView(chip)
            }
            for (j in labels.indices.reversed()) {
                val crt = labels[j]
                val chip = Chip(requireContext())
                chip.text = crt.title
                chip.chipBackgroundColor =
                    ColorStateList.valueOf(ThemeHelper.getColor(requireContext(), crt.colorId))
                chip.isCheckable = true
                chip.chipIcon = ResourcesCompat.getDrawable(resources,R.drawable.ic_check_off, null)
                chip.checkedIcon = ResourcesCompat.getDrawable(resources,R.drawable.ic_check, null)
                chip.id = i++
                if (crt.title == mLabel) {
                    chip.isChecked = true
                }
                binding.labels.addView(chip)
            }
            if (binding.labels.childCount == 0) {
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.labelsView.visibility = View.VISIBLE
            }
        }
        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                if (binding.labels.checkedChipId != -1) {
                    val chip = binding.labels.getChildAt(binding.labels.checkedChipId) as Chip
                    mLabel = chip.text.toString()
                    val color = chip.chipBackgroundColor!!.defaultColor
                    notifyLabelSelected(
                        Label(
                            mLabel,
                            ThemeHelper.getIndexOfColor(requireContext(), color)
                        )
                    )
                } else {
                    notifyLabelSelected(Utils.getInstanceUnlabeledLabel(requireContext()))
                }
                dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, _: Int -> dialog?.dismiss() }
        if (showProfileSelection) {
            builder.setNeutralButton(
                if (preferenceHelper.isUnsavedProfileActive()) resources.getString(R.string.Profile) else preferenceHelper.profile,
                null
            )
            mAlertDialog = builder.create()
            mAlertDialog!!.setOnShowListener {

                //TODO: Clean-up this mess
                val neutral = mAlertDialog!!.getButton(DialogInterface.BUTTON_NEUTRAL)
                neutral.setOnClickListener {
                    val profilesViewModel: ProfilesViewModel by viewModels()
                    val profilesLiveData = profilesViewModel.profiles
                    profilesLiveData.observe(this@SelectLabelDialog) { profiles: List<Profile> ->
                        mProfiles = profiles
                        var profileIdx = 0
                        val arrayAdapter = ArrayAdapter<String>(
                            requireContext(),
                            R.layout.checked_text_view
                        )
                        val pref25 =
                            this@SelectLabelDialog.resources.getText(R.string.pref_profile_default)
                                .toString()
                        val pref52 =
                            this@SelectLabelDialog.resources.getText(R.string.pref_profile_5217)
                                .toString()
                        val crtProfileName = preferenceHelper.profile
                        if (crtProfileName == pref25) {
                            profileIdx = 0
                        } else if (crtProfileName == pref52) {
                            profileIdx = 1
                        }
                        arrayAdapter.add(pref25)
                        arrayAdapter.add(pref52)
                        val predefinedProfileNum = arrayAdapter.count
                        for (i in profiles.indices) {
                            val p = profiles[i]
                            arrayAdapter.add(p.name)
                            if (crtProfileName == p.name) {
                                profileIdx = i + predefinedProfileNum
                            }
                        }
                        val profileDialogBuilder = AlertDialog.Builder(requireContext())
                            .setTitle(this@SelectLabelDialog.resources.getString(R.string.Profile))
                            .setSingleChoiceItems(
                                arrayAdapter,
                                if (preferenceHelper.isUnsavedProfileActive()) -1 else profileIdx
                            ) { dialogInterface: DialogInterface, which: Int ->
                                val selected = arrayAdapter.getItem(which)
                                updateProfile(which)
                                dialogInterface.dismiss()
                                if (mAlertDialog != null) {
                                    mAlertDialog!!.getButton(AlertDialog.BUTTON_NEUTRAL).text =
                                        selected
                                }
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog1: DialogInterface, _: Int -> dialog1.dismiss() }
                        profileDialogBuilder.show()
                    }
                }
            }
        } else {
            mAlertDialog = builder.create()
        }
        return mAlertDialog!!
    }

    private fun notifyLabelSelected(label: Label) {
        mCallback.get()!!.onLabelSelected(label)
    }

    private fun updateProfile(index: Int) {
        when (index) {
            0 -> {
                preferenceHelper.setProfile25to5()
            }
            1 -> {
                preferenceHelper.setProfile52to17()
            }
            else -> {

                preferenceHelper.setProfile(mProfiles[index - PREDEFINED_PROFILES_NR])
            }
        }
    }

    companion object {
        const val PREDEFINED_PROFILES_NR = 2

        @JvmStatic
        fun newInstance(
            listener: OnLabelSelectedListener,
            label: String,
            isExtendedVersion: Boolean,
            showProfileSelection: Boolean = false
        ): SelectLabelDialog {
            val dialog = SelectLabelDialog()
            dialog.mCallback = WeakReference(listener)
            dialog.mLabel = label
            dialog.mIsExtendedVersion = isExtendedVersion
            dialog.showProfileSelection = showProfileSelection
            return dialog
        }
    }
}