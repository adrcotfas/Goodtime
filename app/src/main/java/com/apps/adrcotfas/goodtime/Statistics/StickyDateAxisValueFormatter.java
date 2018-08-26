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

    private List<LocalDate> dates;
    private LineChart chart;
    private TextView sticky;
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("MMM");

    StickyDateAxisValueFormatter(List<LocalDate> dates, LineChart chart, TextView sticky) {
        this.dates = dates;
        this.chart = chart;
        this.sticky = sticky;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // in case of invalid values
        if(value < 0) {
            return "";
        }

        // set the sticky TextView
        int firstValue = (int)chart.getXAxis().mEntries[0];
        if (firstValue < 0 || firstValue >= dates.size()) {
            return "";
        }
        LocalDate firstDate = dates.get(firstValue);
        int stickyMonth = firstDate.getMonthOfYear();
        int stickyYear = firstDate.getYear();
        final String stickyText = firstDate.toString(monthFormatter) + "\n" + stickyYear;
        sticky.setText(stickyText);

        LocalDate dateTime = dates.get((int)value);
        int dayOfMonth = dateTime.getDayOfMonth();
        int month = dateTime.getMonthOfYear();

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