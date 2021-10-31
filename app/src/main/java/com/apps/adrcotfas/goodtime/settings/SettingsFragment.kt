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
package com.apps.adrcotfas.goodtime.settings

import com.apps.adrcotfas.goodtime.util.BatteryUtils.Companion.isIgnoringBatteryOptimizations
import com.apps.adrcotfas.goodtime.util.UpgradeDialogHelper.Companion.launchUpgradeDialog
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import android.os.Bundle
import com.apps.adrcotfas.goodtime.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.annotation.TargetApi
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import androidx.preference.*
import com.apps.adrcotfas.goodtime.ui.common.TimePickerDialogBuilder
import com.apps.adrcotfas.goodtime.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import xyz.aprildown.ultimateringtonepicker.RingtonePickerDialog
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), OnRequestPermissionsResultCallback {
    private lateinit var prefDisableSoundCheckbox: CheckBoxPreference
    private lateinit var prefDndMode: CheckBoxPreference

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        setupReminderPreference()
        setupStartOfDayPreference()
        prefDisableSoundCheckbox = findPreference(PreferenceHelper.DISABLE_SOUND_AND_VIBRATION)!!
        prefDndMode = findPreference(PreferenceHelper.DND_MODE)!!
    }

    private fun setupTimePickerPreference(
        preference: Preference,
        getTimeAction: () -> Int,
        setTimeAction: (Int) -> Unit
    ) {
        preference.setOnPreferenceClickListener {
            val dialog = TimePickerDialogBuilder(requireContext())
                .buildDialog(LocalTime.ofSecondOfDay(getTimeAction().toLong()))
            dialog.addOnPositiveButtonClickListener {
                val newValue = LocalTime.of(dialog.hour, dialog.minute).toSecondOfDay()
                setTimeAction(newValue)
                updateTimePickerPreferenceSummary(preference, getTimeAction())
            }
            dialog.showOnce(parentFragmentManager, "MaterialTimePicker")
            true
        }
        updateTimePickerPreferenceSummary(preference, getTimeAction())
    }

    private fun setupReminderPreference() {
        // restore from preferences
        val dayOfWeekPref = findPreference<DayOfWeekPreference>(PreferenceHelper.REMINDER_DAYS)
        dayOfWeekPref!!.setCheckedDays(
            preferenceHelper.getBooleanArray(
                PreferenceHelper.REMINDER_DAYS, DayOfWeek.values().size
            )
        )

        val timePickerPref: Preference = findPreference(PreferenceHelper.REMINDER_TIME)!!
        setupTimePickerPreference(
            timePickerPref,
            { preferenceHelper.getReminderTime() },
            { preferenceHelper.setReminderTime(it) })
    }

    private fun setupStartOfDayPreference() {
        val pref: Preference = findPreference(PreferenceHelper.WORK_DAY_START)!!
        setupTimePickerPreference(
            pref,
            { preferenceHelper.getStartOfDay() },
            { preferenceHelper.setStartOfDay(it) })
    }

    private fun updateTimePickerPreferenceSummary(pref: Preference, secondOfDay: Int) {
        pref.summary = secondsOfDayToTimerFormat(
            secondOfDay, DateFormat.is24HourFormat(context)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view!!.alpha = 0f
        view.animate().alpha(1f).duration = 100
        return view
    }

    @SuppressLint("BatteryLife")
    override fun onResume() {
        super.onResume()
        requireActivity().title = getString(R.string.settings)
        setupTheme()
        setupRingtone()
        setupScreensaver()
        setupAutoStartSessionVsInsistentNotification()
        setupDisableSoundCheckBox()
        setupDnDCheckBox()
        setupFlashingNotificationPref()
        setupOneMinuteLeftNotificationPref()
        val disableBatteryOptimizationPref =
            findPreference<Preference>(PreferenceHelper.DISABLE_BATTERY_OPTIMIZATION)
        if (!isIgnoringBatteryOptimizations(requireContext())) {
            disableBatteryOptimizationPref!!.isVisible = true
            disableBatteryOptimizationPref.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:" + requireActivity().packageName)
                    startActivity(intent)
                    true
                }
        } else {
            disableBatteryOptimizationPref!!.isVisible = false
        }
        findPreference<Preference>(PreferenceHelper.DISABLE_WIFI)!!.isVisible =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        prefDndMode.isVisible = true
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference.key) {
            PreferenceHelper.TIMER_STYLE -> {
                super.onDisplayPreferenceDialog(preference)
            }
            PreferenceHelper.VIBRATION_TYPE -> {
                val dialog = VibrationPreferenceDialogFragment.newInstance(preference.key)
                dialog.setTargetFragment(this, 0)
                dialog.showOnce(parentFragmentManager, "VibrationType")
            }
            else -> {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    private fun setupAutoStartSessionVsInsistentNotification() {
        // Continuous mode versus insistent notification
        val autoWork = findPreference<CheckBoxPreference>(PreferenceHelper.AUTO_START_WORK)
        autoWork!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val pref = findPreference<CheckBoxPreference>(PreferenceHelper.INSISTENT_RINGTONE)
                if (newValue as Boolean) {
                    pref!!.isChecked = false
                }
                true
            }
        val autoBreak = findPreference<CheckBoxPreference>(PreferenceHelper.AUTO_START_BREAK)
        autoBreak!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val pref = findPreference<CheckBoxPreference>(PreferenceHelper.INSISTENT_RINGTONE)
                if (newValue as Boolean) {
                    pref!!.isChecked = false
                }
                true
            }
        val insistentRingPref =
            findPreference<CheckBoxPreference>(PreferenceHelper.INSISTENT_RINGTONE)
        insistentRingPref!!.onPreferenceClickListener =
            if (preferenceHelper.isPro()) null else Preference.OnPreferenceClickListener {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
                insistentRingPref.isChecked = false
                true
            }
        insistentRingPref.onPreferenceChangeListener =
            if (preferenceHelper.isPro()) Preference.OnPreferenceChangeListener { _, newValue: Any ->
                val p1 = findPreference<CheckBoxPreference>(PreferenceHelper.AUTO_START_BREAK)
                val p2 = findPreference<CheckBoxPreference>(PreferenceHelper.AUTO_START_WORK)
                if (newValue as Boolean) {
                    p1!!.isChecked = false
                    p2!!.isChecked = false
                }
                true
            } else null
    }

    private fun handleRingtonePrefClick(preference: Preference) {
        if (preference.key == PreferenceHelper.RINGTONE_BREAK_FINISHED && !preferenceHelper.isPro()) {
            launchUpgradeDialog(requireActivity().supportFragmentManager)
        } else {
            val selectedUri =
                Uri.parse(toRingtone(preferenceHelper.getNotificationSoundFinished(preference.key)!!).uri)
            val settings = UltimateRingtonePicker.Settings(
                preSelectUris = listOf(selectedUri),
                systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                    customSection = UltimateRingtonePicker.SystemRingtonePicker.CustomSection(),
                    defaultSection = UltimateRingtonePicker.SystemRingtonePicker.DefaultSection(
                        showSilent = false
                    ),
                    ringtoneTypes = listOf(
                        RingtoneManager.TYPE_NOTIFICATION,
                    )
                ),
                deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                    alwaysUseSaf = true
                )
            )
            val dialog = RingtonePickerDialog.createEphemeralInstance(
                settings = settings,
                dialogTitle = preference.title,
                listener = object : UltimateRingtonePicker.RingtonePickerListener {
                    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
                        preference.apply {
                            val ringtone = if (ringtones.isNotEmpty()) {
                                Ringtone(ringtones.first().uri.toString(), ringtones.first().name)
                            } else {
                                Ringtone("", resources.getString(R.string.pref_ringtone_summary))
                            }
                            val json = Gson().toJson(ringtone)
                            preferenceHelper.setNotificationSoundFinished(preference.key, json)
                            summary = ringtone.name
                            callChangeListener(ringtone)
                        }
                    }
                }
            )
            dialog.showOnce(parentFragmentManager, "RingtonePref")
        }
    }

    private fun setupRingtone() {
        val prefWork = findPreference<Preference>(PreferenceHelper.RINGTONE_WORK_FINISHED)
        val prefBreak = findPreference<Preference>(PreferenceHelper.RINGTONE_BREAK_FINISHED)
        val defaultSummary = resources.getString(
            R.string.pref_ringtone_summary
        )
        prefWork!!.summary =
            toRingtone(preferenceHelper.getNotificationSoundWorkFinished()!!, defaultSummary).name
        prefBreak!!.summary =
            toRingtone(preferenceHelper.getNotificationSoundBreakFinished()!!, defaultSummary).name

        prefWork.setOnPreferenceClickListener {
            handleRingtonePrefClick(prefWork)
            true
        }
        prefBreak.setOnPreferenceClickListener {
            handleRingtonePrefClick(prefBreak)
            true
        }

        if (preferenceHelper.isPro()) {
            prefWork.onPreferenceChangeListener = null
        } else {
            preferenceHelper.setNotificationSoundBreakFinished(preferenceHelper.getNotificationSoundWorkFinished()!!)
            prefWork.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue: Any? ->
                    preferenceHelper.setNotificationSoundBreakFinished(Gson().toJson(newValue))
                    prefBreak.summary = prefWork.summary
                    true
                }
        }
        val prefEnableRingtone =
            findPreference<SwitchPreferenceCompat>(PreferenceHelper.ENABLE_RINGTONE)
        toggleEnableRingtonePreference(prefEnableRingtone!!.isChecked)
        prefEnableRingtone.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue: Any ->
                toggleEnableRingtonePreference(newValue as Boolean)
                true
            }
    }

    private fun setupScreensaver() {
        val screensaverPref =
            findPreference<CheckBoxPreference>(PreferenceHelper.ENABLE_SCREENSAVER_MODE)
        findPreference<Preference>(PreferenceHelper.ENABLE_SCREENSAVER_MODE)!!.onPreferenceClickListener =
            if (preferenceHelper.isPro()) null else Preference.OnPreferenceClickListener {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
                screensaverPref!!.isChecked = false
                true
            }
        findPreference<Preference>(PreferenceHelper.ENABLE_SCREEN_ON)!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                if (!(newValue as Boolean)) {
                    if (screensaverPref!!.isChecked) {
                        screensaverPref.isChecked = false
                    }
                }
                true
            }
    }

    private fun setupTheme() {
        val prefAmoled = findPreference<SwitchPreferenceCompat>(PreferenceHelper.AMOLED)
        prefAmoled!!.onPreferenceClickListener =
            if (preferenceHelper.isPro()) null else Preference.OnPreferenceClickListener {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
                prefAmoled.isChecked = true
                true
            }
        prefAmoled.onPreferenceChangeListener =
            if (preferenceHelper.isPro()) Preference.OnPreferenceChangeListener { _, _ ->
                ThemeHelper.setTheme(activity as SettingsActivity, preferenceHelper.isAmoledTheme())
                requireActivity().recreate()
                true
            } else null
    }

    private fun updateDisableSoundCheckBoxSummary(
        pref: CheckBoxPreference?,
        notificationPolicyAccessGranted: Boolean
    ) {
        if (notificationPolicyAccessGranted) {
            pref!!.summary = ""
        } else {
            pref!!.setSummary(R.string.settings_grant_permission)
        }
    }

    private fun setupDisableSoundCheckBox() {
        if (isNotificationPolicyAccessDenied) {
            updateDisableSoundCheckBoxSummary(prefDisableSoundCheckbox, false)
            prefDisableSoundCheckbox.isChecked = false
            prefDisableSoundCheckbox.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    requestNotificationPolicyAccess()
                    false
                }
        } else {
            updateDisableSoundCheckBoxSummary(prefDisableSoundCheckbox, true)
            prefDisableSoundCheckbox.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (prefDndMode.isChecked) {
                        prefDndMode.isChecked = false
                        true
                    } else {
                        false
                    }
                }
        }
    }

    private fun setupFlashingNotificationPref() {
        val pref =
            findPreference<SwitchPreferenceCompat>(PreferenceHelper.ENABLE_FLASHING_NOTIFICATION)
        pref!!.onPreferenceClickListener =
            if (preferenceHelper.isPro()) null else Preference.OnPreferenceClickListener {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
                pref.isChecked = false
                true
            }
    }

    private fun setupOneMinuteLeftNotificationPref() {
        val pref =
            findPreference<SwitchPreferenceCompat>(PreferenceHelper.ENABLE_ONE_MINUTE_BEFORE_NOTIFICATION)
        pref!!.onPreferenceClickListener =
            if (preferenceHelper.isPro()) null else Preference.OnPreferenceClickListener {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
                pref.isChecked = false
                true
            }
    }

    private fun setupDnDCheckBox() {
        if (isNotificationPolicyAccessDenied) {
            updateDisableSoundCheckBoxSummary(prefDndMode, false)
            prefDndMode.isChecked = false
            prefDndMode.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    requestNotificationPolicyAccess()
                    false
                }
        } else {
            updateDisableSoundCheckBoxSummary(prefDndMode, true)
            prefDndMode.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (prefDisableSoundCheckbox.isChecked) {
                        prefDisableSoundCheckbox.isChecked = false
                        true
                    } else {
                        false
                    }
                }
        }
    }

    private fun requestNotificationPolicyAccess() {
        if (isNotificationPolicyAccessDenied) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    @get:TargetApi(Build.VERSION_CODES.M)
    private val isNotificationPolicyAccessDenied: Boolean
        get() {
            val notificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return !notificationManager.isNotificationPolicyAccessGranted
        }

    private fun toggleEnableRingtonePreference(newValue: Boolean) {
        findPreference<Preference>(PreferenceHelper.RINGTONE_WORK_FINISHED)!!.isVisible =
            newValue
        findPreference<Preference>(PreferenceHelper.RINGTONE_BREAK_FINISHED)!!.isVisible =
            newValue
        findPreference<Preference>(PreferenceHelper.PRIORITY_ALARM)!!.isVisible =
            newValue
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}