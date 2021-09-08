package com.apps.adrcotfas.goodtime.util

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Will show the fragment once. Use this to avoid double dialogs when fast clicking.
 * The disadvantage is that the dialog is created even if it won't be used.
 */
fun DialogFragment.showOnce(
    @NonNull manager: FragmentManager,
    @Nullable tag: String
) {
    if (manager.findFragmentByTag(tag) == null) {
        this.show(manager, tag)
    }
}
