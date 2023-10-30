package com.apps.adrcotfas.goodtime.util

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Will show the fragment once. Use this to avoid double dialogs when fast clicking.
 * The disadvantage is that the dialog is created even if it won't be used.
 */
fun DialogFragment.showOnce(
    manager: FragmentManager,
    tag: String
) {
    if (manager.findFragmentByTag(tag) == null) {
        this.show(manager, tag)
    }
}
