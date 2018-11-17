package com.apps.adrcotfas.goodtime.Statistics.Main;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatLong;

public class CustomYAxisFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return formatLong((long)value);
    }
}
