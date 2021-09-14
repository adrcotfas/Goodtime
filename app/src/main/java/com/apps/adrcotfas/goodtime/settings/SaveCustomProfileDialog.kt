package com.apps.adrcotfas.goodtime.settings

import androidx.preference.PreferenceDialogFragmentCompat
import android.annotation.SuppressLint
import android.app.Dialog
import com.apps.adrcotfas.goodtime.R
import android.widget.EditText
import android.widget.Toast
import android.text.TextUtils
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import com.apps.adrcotfas.goodtime.database.Profile
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class SaveCustomProfileDialog : PreferenceDialogFragmentCompat() {

    private val viewModel: ProfilesViewModel by viewModels()

    private lateinit var profileToAdd: Profile
    private lateinit var title: String
    private lateinit var crtProfileName: String
    private val profiles: MutableList<String> = ArrayList()

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        val profilePreference = preference as ListPreference

        @SuppressLint("InflateParams") val dialogView =
            layoutInflater.inflate(R.layout.dialog_set_profile_name, null)
        builder.setView(dialogView)
        builder.setTitle(title)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val input = dialogView.findViewById<EditText>(R.id.value)
            var name = input.text.toString()
            if (profiles.contains(name)) {
                Toast.makeText(context, R.string.profile_already_exists, Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            name = name.trim { it <= ' ' }
            if (!TextUtils.isEmpty(name)) {
                profileToAdd.name = name
                viewModel.addProfile(profileToAdd)
            }
            if (crtProfileName != name) {
                profilePreference.value = name
                profilePreference.summary = name
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ -> }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // do nothing
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return d
    }

    companion object {
        fun newInstance(
            key: String,
            title: String,
            profileToAdd: Profile,
            preference: ProfilePreference
        ): SaveCustomProfileDialog {
            val fragment = SaveCustomProfileDialog()
            fragment.profileToAdd = profileToAdd
            fragment.title = title
            fragment.crtProfileName = preference.value
            for (c in preference.entries) {
                fragment.profiles.add(c.toString())
            }
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}