package com.apps.adrcotfas.goodtime.Statistics;

import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class StickyDateAxisValueFormatter implements IAxisValueFormatter {

    final private List<LocalDate> mDates;
    private LineChart mChart;
    private TextView mStickyText;
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("MMM");

    StickyDateAxisValueFormatter(List<LocalDate> mDates, LineChart mChart, TextView mStickyText) {
        this.mDates = mDates;
        this.mChart = mChart;
        this.mStickyText = mStickyText;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        final int firstValue = (int) mChart.getXAxis().mEntries[0];
        // in case of invalid values
        if(value < 0 || value >= mDates.size() || firstValue < 0 || firstValue >= mDates.size()) {
            return "";
        }

        // set the sticky TextView
        final LocalDate firstDate = mDates.get(firstValue);
        final int stickyMonth = firstDate.getMonthOfYear();
        final int stickyYear = firstDate.getYear();
        final String stickyText = firstDate.toString(monthFormatter) + "\n" + stickyYear;
        mStickyText.setText(stickyText);

        final LocalDate dateTime = mDates.get((int)value);
        final int dayOfMonth = dateTime.getDayOfMonth();
        final int month = dateTime.getMonthOfYear();

        String ret;
        if(stickyMonth != month && dayOfMonth == 1) {
            ret = dateTime.toString(monthFormatter);
        }
        else {
            ret = Integer.toString(dayOfMonth);
        }
        return ret;
    }
}