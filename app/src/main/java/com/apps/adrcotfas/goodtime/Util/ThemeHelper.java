package com.apps.adrcotfas.goodtime.Util;

import android.app.ActionBar;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void styleChip(Context context, Chip chip) {
        chip.setLayoutParams(new ChipGroup.LayoutParams(ChipGroup.LayoutParams.WRAP_CONTENT, dpToPx(context, 24)));
        chip.setChipIconSize(ThemeHelper.dpToPx(context,20));
        chip.setChipStartPadding(ThemeHelper.dpToPx(context,3));
        chip.setChipCornerRadius(ThemeHelper.dpToPx(context,20));
        chip.setTextStartPadding(ThemeHelper.dpToPx(context,8));
        chip.setTextEndPadding(ThemeHelper.dpToPx(context,4));
        chip.setTextAppearance(R.style.ChipTextAppearance);
    }
}
