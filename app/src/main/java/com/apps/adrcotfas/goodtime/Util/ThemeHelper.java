package com.apps.adrcotfas.goodtime.Util;

import androidx.appcompat.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;

public class ThemeHelper {

    public static void setTheme(AppCompatActivity activity) {

        final int dark = activity.getResources().getColor(R.color.pref_dark);
        final int blue = activity.getResources().getColor(R.color.pref_blue);
        final int red = activity.getResources().getColor(R.color.pref_red);

        int i = PreferenceHelper.getTheme();
        if (i == dark) {
            activity.setTheme(R.style.Dark);
        } else if (i == blue) {
            activity.setTheme(R.style.Blue);
        } else if (i == red) {
            activity.setTheme(R.style.Red);
        } else {
            activity.setTheme(R.style.Classic);
        }
    }
}
