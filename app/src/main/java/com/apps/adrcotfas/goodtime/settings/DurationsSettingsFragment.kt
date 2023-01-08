package com.apps.adrcotfas.goodtime.settings

import com.apps.adrcotfas.goodtime.util.UpgradeDialogHelper.Companion.launchUpgradeDialog
import dagger.hilt.android.AndroidEntryPoint
import androidx.preference.PreferenceFragmentCompat
import com.apps.adrcotfas.goodtime.settings.ProfilePreference.ProfileChangeListener
import androidx.preference.SwitchPreferenceCompat
import javax.inject.Inject
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.apps.adrcotfas.goodtime.R
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.apps.adrcotfas.goodtime.database.Profile
import com.apps.adrcotfas.goodtime.util.Constants
import com.apps.adrcotfas.goodtime.util.showOnce
import java.util.ArrayList

@AndroidEntryPoint
class DurationsSettingsFragment : PreferenceFragmentCompat(), ProfileChangeListener,
    SeekBarPreferenceDialog.Listener {

    private lateinit var prefProfile: ProfilePreference
    private lateinit var prefWorkDuration: SeekBarPreference
    private lateinit var prefBreakDuration: SeekBarPreference
    private lateinit var prefEnableLongBreak: SwitchPreferenceCompat
    private lateinit var prefLongBreakDuration: SeekBarPreference
    private lateinit var prefSessionsBeforeLongBreak: SeekBarPreference
    private var profiles: List<Profile> = ArrayList()

    private val viewModel: ProfilesViewModel by viewModels()

    private lateinit var saveCustomProfileButton: Preference

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.alpha = 0f
        view.animate().alpha(1f).duration = 150
        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_durations, rootKey)
        viewModel.profiles.observe(this) { profiles: List<Profile> ->
            this.profiles = profiles
            setupProfiles()
        }
        setupDurations()
        saveCustomProfileButton = findPreference(PreferenceHelper.SAVE_CUSTOM_PROFILE)!!
        saveCustomProfileButton.isVisible = preferenceHelper.isUnsavedProfileActive()
    }

    private fun setupDurations() {
        prefWorkDuration = findPreference(PreferenceHelper.WORK_DURATION)!!
        prefWorkDuration.showSeekBarValue = true
        prefWorkDuration.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                prefProfile.summary = ""
                onDurationsChanged()
                true
            }
        prefBreakDuration = findPreference(PreferenceHelper.BREAK_DURATION)!!
        prefBreakDuration.showSeekBarValue = true
        prefBreakDuration.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                prefProfile.summary = ""
                onDurationsChanged()
                true
            }
        prefEnableLongBreak = findPreference(PreferenceHelper.ENABLE_LONG_BREAK)!!
        toggleLongBreakPreference(prefEnableLongBreak.isChecked)
        prefEnableLongBreak.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                toggleLongBreakPreference(newValue as Boolean)
                prefProfile.summary = ""
                onDurationsChanged()
                true
            }
        prefLongBreakDuration = findPreference(PreferenceHelper.LONG_BREAK_DURATION)!!
        prefLongBreakDuration.showSeekBarValue = true
        prefLongBreakDuration.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                prefProfile.summary = ""
                onDurationsChanged()
                true
            }
        prefSessionsBeforeLongBreak = findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK)!!
        prefSessionsBeforeLongBreak.showSeekBarValue = true
        prefSessionsBeforeLongBreak.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                prefProfile.summary = ""
                onDurationsChanged()
                true
            }
        listOf(
            prefWorkDuration,
            prefBreakDuration,
            prefLongBreakDuration,
            prefSessionsBeforeLongBreak
        ).forEach { it ->
            it.setOnPreferenceClickListener {
                SeekBarPreferenceDialog.newInstance(it as SeekBarPreference, this)
                    .showOnce(parentFragmentManager, "SeekbarPreference")
                true
            }
        }
    }

    private fun setupProfiles() {
        prefProfile = findPreference(PreferenceHelper.PROFILE)!!
        prefProfile.attachListener(this)
        val profileNames = ArrayList<CharSequence>()
        val profileName25To5 = resources.getText(R.string.pref_profile_default)
        val profileName52to17 = resources.getText(R.string.pref_profile_5217)
        profileNames.add(profileName25To5)
        profileNames.add(profileName52to17)
        for (p in profiles) {
            // TODO: workaround for implementation fault: custom profiles with the same name as the default ones
            //  delete this in future releases
            if (p.name == profileName25To5.toString() || p.name == profileName52to17.toString()) {
                viewModel.deleteProfile(p.name)
                continue
            }
            profileNames.add(p.name)
        }
        prefProfile.entries = profileNames.toTypedArray()
        prefProfile.entryValues = profileNames.toTypedArray()
        if (preferenceHelper.isUnsavedProfileActive()) {
            prefProfile.summary = ""
        }
        prefProfile.isEnabled = true
    }

    private fun toggleLongBreakPreference(newValue: Boolean) {
        findPreference<Preference>(PreferenceHelper.LONG_BREAK_DURATION)!!.isVisible = newValue
        findPreference<Preference>(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK)!!.isVisible =
            newValue
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference.key == PreferenceHelper.SAVE_CUSTOM_PROFILE) {
            if (preferenceHelper.isPro()) {
                val profile = if (prefEnableLongBreak.isChecked) Profile(
                    "",
                    prefWorkDuration.value,
                    prefBreakDuration.value,
                    prefLongBreakDuration.value,
                    prefSessionsBeforeLongBreak.value
                ) else Profile(
                    "",
                    prefWorkDuration.value,
                    prefBreakDuration.value
                )
                val dialog = SaveCustomProfileDialog.newInstance(
                    PreferenceHelper.PROFILE,
                    getString(R.string.pref_save_custom_profile),
                    profile,
                    prefProfile
                )
                dialog.setTargetFragment(this, 0)
                dialog.showOnce(parentFragmentManager, "SaveCustomProfile")
            } else {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
            }
        } else if (preference.key == PreferenceHelper.PROFILE) {
            val dialog = ProfileSelectDialog.newInstance(PreferenceHelper.PROFILE)
            dialog.setTargetFragment(this, 0)
            dialog.showOnce(parentFragmentManager, "ProfileSelect")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun onDurationsChanged() {
        preferenceHelper.setUnsavedProfileActive(true)
        saveCustomProfileButton.isVisible = true
    }

    override fun onProfileChange(newValue: CharSequence?) {
        preferenceHelper.setUnsavedProfileActive(false)
        saveCustomProfileButton.isVisible = false
        if (newValue == resources.getText(R.string.pref_profile_default)) {
            prefWorkDuration.value = Constants.DEFAULT_WORK_DURATION_DEFAULT
            prefBreakDuration.value = Constants.DEFAULT_BREAK_DURATION_DEFAULT
            prefEnableLongBreak.isChecked = false
            toggleLongBreakPreference(false)
            prefLongBreakDuration.value = Constants.DEFAULT_LONG_BREAK_DURATION
            prefSessionsBeforeLongBreak.value = Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK
        } else if (newValue == resources.getText(R.string.pref_profile_5217)) {
            prefWorkDuration.value = Constants.DEFAULT_WORK_DURATION_5217
            prefBreakDuration.value = Constants.DEFAULT_BREAK_DURATION_5217
            prefEnableLongBreak.isChecked = false
            toggleLongBreakPreference(false)
        } else {
            for (p in profiles) {
                if (newValue == p.name) {
                    prefWorkDuration.value = p.durationWork
                    prefBreakDuration.value = p.durationBreak
                    prefEnableLongBreak.isChecked = p.enableLongBreak
                    toggleLongBreakPreference(p.enableLongBreak)
                    prefLongBreakDuration.value = p.durationLongBreak
                    prefSessionsBeforeLongBreak.value = p.sessionsBeforeLongBreak
                    break
                }
            }
        }
    }

    override fun onValueSet() {
        saveCustomProfileButton.isVisible = true
        prefProfile.summary = ""
    }
}