package com.apps.adrcotfas.goodtime.Util;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;

public class ThemeHelper {
    public static void setTheme(AppCompatActivity activity) {
        int theme = R.style.AppTheme;
        switch (PreferenceHelper.getTheme()) {
            case Color.BLACK:
                theme = R.style.AppThemeDark;
                break;
            default:
                break;
        }
        activity.setTheme(theme);
    }
}
