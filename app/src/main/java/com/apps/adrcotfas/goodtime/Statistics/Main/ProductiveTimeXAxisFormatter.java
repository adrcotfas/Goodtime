package com.apps.adrcotfas.goodtime.Statistics.Main;

import com.apps.adrcotfas.goodtime.Util.StringUtils;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import static com.apps.adrcotfas.goodtime.Statistics.Main.SpinnerProductiveTimeType.DAY_OF_WEEK;
import static com.apps.adrcotfas.goodtime.Statistics.Main.SpinnerProductiveTimeType.HOUR_OF_DAY;


/**
 * Custom formatter used to print the day of the week or the hour of the day.
 */
public class ProductiveTimeXAxisFormatter implements IAxisValueFormatter {

    private SpinnerProductiveTimeType mType;
    ProductiveTimeXAxisFormatter(SpinnerProductiveTimeType type) {
        mType = type;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if ((mType == HOUR_OF_DAY) && (value < 24) && (value >= 0)) {
            return StringUtils.toHourOfDay((int) value);
        } else if ((mType == DAY_OF_WEEK) && (value < 7) && (value >= 0)) {
            return StringUtils.toDayOfWeek((int) (value + 1));
        } else {
            return "";
        }
    }
}
