package com.apps.adrcotfas.goodtime.Statistics;

import android.content.Context;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;

public class Utils {
    public static LabelAndColor getInstanceTotalLabel(Context context) {
        return new LabelAndColor("total", context.getResources().getColor(R.color.dayNightTeal));
    }

    public static LabelAndColor getInstanceUnlabeledLabel(Context context) {
        return new LabelAndColor("unlabeled", context.getResources().getColor(R.color.white));
    }
}
