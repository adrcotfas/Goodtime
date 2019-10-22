package com.apps.adrcotfas.goodtime.Settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.ListPreference;

import com.apps.adrcotfas.goodtime.R;

public class ProfilePreference extends ListPreference {

    public interface ProfileChangeListener {
        void onProfileChange(CharSequence newValue);
    }

    private ProfileChangeListener mChangeListener;

    public ProfilePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ProfilePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProfilePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
    }

    public ProfilePreference(Context context) {
        this(context, null);
    }


    public void attachListener(ProfileChangeListener changeListener) {
        mChangeListener = changeListener;
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (mChangeListener != null){
            mChangeListener.onProfileChange(getValue());
        }
    }
}
