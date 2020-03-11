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
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ThemeHelper {

    public static final int COLOR_INDEX_UNLABELED = 0;
    public static final int COLOR_INDEX_ALL_LABELS = 42;
    public static final int COLOR_INDEX_BREAK = 43;

    public static void setTheme(AppCompatActivity activity) {
        if (PreferenceHelper.isAmoledTheme()) {
            activity.setTheme(R.style.AppTheme);
        } else {
            activity.setTheme(R.style.AppThemeDark);
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
        chip.setChipStartPadding(ThemeHelper.dpToPx(context,4));
        chip.setChipCornerRadius(ThemeHelper.dpToPx(context,12));
        chip.setTextStartPadding(ThemeHelper.dpToPx(context,4));
        chip.setTextEndPadding(ThemeHelper.dpToPx(context,4));
        chip.setTextAppearance(context, R.style.ChipTextAppearance);
        chip.setChipIcon(context.getResources().getDrawable(R.drawable.ic_check_off));
        chip.setCheckedIcon(context.getResources().getDrawable(R.drawable.ic_check));
        chip.setEllipsize(TextUtils.TruncateAt.END);
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

    public static int[] getPalette(Context context)
    {
        int resourceId = getResource(R.attr.palette, context);
        if (resourceId < 0) throw new RuntimeException("resource not found");

        return context.getResources().getIntArray(resourceId);
    }

    private static int getResource(@AttrRes int attrId, Context context)
    {
        TypedArray ta = getTypedArray(attrId, context);
        int resourceId = ta.getResourceId(0, -1);
        ta.recycle();

        return resourceId;
    }

    private static TypedArray getTypedArray(@AttrRes int attrId, Context context)
    {
        int[] attrs = new int[]{ attrId };
        return context.obtainStyledAttributes(attrs);
    }

    public static int getColor(Context context, int colorIndex) {
        if (colorIndex == COLOR_INDEX_UNLABELED || colorIndex == -1) {
            return context.getResources().getColor(R.color.white);
        }
        if (colorIndex == COLOR_INDEX_ALL_LABELS) {
            return context.getResources().getColor(R.color.teal200);
        }
        if (colorIndex == COLOR_INDEX_BREAK) {
            return context.getResources().getColor(R.color.grey_500);
        }
        int colors[] = getPalette(context);
        if (colorIndex < colors.length) {
            return colors[colorIndex];
        }

        return context.getResources().getColor(R.color.white);
    }

    public static int getIndexOfColor(Context context, int color) {
        if (color == context.getResources().getColor(R.color.white)) {
            return COLOR_INDEX_UNLABELED;
        }
        if (color == context.getResources().getColor(R.color.teal200)) {
            return COLOR_INDEX_ALL_LABELS;
        }

        int colors[] = getPalette(context);
        for (int i = 0; i <colors.length; ++i) {
            if (color == colors[i]) {
                return i;
            }
        }
        return COLOR_INDEX_UNLABELED;
    }

}
