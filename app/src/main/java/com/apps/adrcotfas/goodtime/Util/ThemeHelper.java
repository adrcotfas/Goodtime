package com.apps.adrcotfas.goodtime.Util;

import android.support.v7.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;

public class ThemeHelper {

    public static void setTheme(AppCompatActivity activity) {

        final int dark = activity.getResources().getColor(R.color.pref_dark);
        final int light = activity.getResources().getColor(R.color.pref_light);

        int i = PreferenceHelper.getTheme();
        if (i == light) {
            activity.setTheme(R.style.Light);

        } else if (i == dark) {
            activity.setTheme(R.style.Dark);

        } else {
            activity.setTheme(R.style.Classic);

        }
    }
}
