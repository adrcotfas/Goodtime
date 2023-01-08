package com.apps.adrcotfas.goodtime.settings

import kotlin.jvm.JvmOverloads
import com.apps.adrcotfas.goodtime.settings.ProfilePreference.ProfileChangeListener
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.ListPreference
import com.apps.adrcotfas.goodtime.R

class ProfilePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int = 0
) : ListPreference(context, attrs, defStyleAttr, defStyleRes) {
    interface ProfileChangeListener {
        fun onProfileChange(newValue: CharSequence?)
    }

    private var mChangeListener: ProfileChangeListener? = null

    @SuppressLint("RestrictedApi")
    constructor(context: Context, attrs: AttributeSet?) : this(
        context, attrs, TypedArrayUtils.getAttr(
            context, R.attr.dialogPreferenceStyle,
            android.R.attr.dialogPreferenceStyle
        )
    ) {
    }

    constructor(context: Context) : this(context, null)

    fun attachListener(changeListener: ProfileChangeListener?) {
        mChangeListener = changeListener
    }

    override fun setValue(value: String) {
        super.setValue(value)
        if (mChangeListener != null) {
            mChangeListener!!.onProfileChange(getValue())
        }
    }
}