/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Util;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;


public class ThemeHelper {

    public static void setTheme(AppCompatActivity activity) {

        final int dark = activity.getResources().getColor(R.color.darkIndigo1100);
        final int blue = activity.getResources().getColor(R.color.blue1100);

        int i = PreferenceHelper.getTheme();

        if (i == dark) {
            activity.setTheme(R.style.AppThemeDark);
        } else if (i == blue) {
            activity.setTheme(R.style.AppThemeBlue);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }

    public static float pxToDp(Context context, int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int spToPx(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int dpToSp(Context context, float dp) {
        return (int) (dpToPx(context, dp) / context.getResources().getDisplayMetrics().scaledDensity);
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

    public static void clearFocusEditText(View v, Context context) {
        v.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void requestFocusEditText(EditText v, Context context) {
        v.setSelection(v.length());
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }
}
