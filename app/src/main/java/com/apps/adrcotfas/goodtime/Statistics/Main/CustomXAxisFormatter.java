package com.apps.adrcotfas.goodtime.Statistics.Main;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Custom X-axis formatter which marks the months and years for an easier reading of the chart:
 * - formats the X-axis labels according to the current range type view (days, weeks or months)
 * - formats the first X-axis label to the current month and year,
 * - formats the first day of a month to the corresponding month
 */
public class CustomXAxisFormatter implements IAxisValueFormatter {

    final private List<LocalDate> mDates;
    private SpinnerRangeType mRangeType;
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("MMM");

    CustomXAxisFormatter(List<LocalDate> dates, SpinnerRangeType rangeType) {
        mDates = dates;
        mRangeType = rangeType;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        final int firstValue = (int) axis.mEntries[0];
        final boolean isLeftmost = value == axis.mEntries[0];

        // in case of invalid values
        if(value < 0 || value >= mDates.size() || firstValue < 0 || firstValue >= mDates.size()) {
            return "";
        }

        final LocalDate stickyDate = mDates.get(firstValue);
        final int stickyMonth = stickyDate.getMonthOfYear();
        final int stickyYear = stickyDate.getYear();
        final String stickyText = stickyDate.toString(monthFormatter) + "\n" + stickyYear;

        final LocalDate crtDate = mDates.get((int)value);
        final int crtDay = crtDate.getDayOfMonth();
        final int crtMonth = crtDate.getMonthOfYear();

        String result;

        if (isLeftmost) {
            result = stickyText;
        } else {
            switch (mRangeType) {
                case DAYS:
                    if(crtDay == 1 && crtMonth != stickyMonth) {
                        result = crtDate.toString(monthFormatter);
                        if (crtDate.getMonthOfYear() == 1 && crtDate.getMonthOfYear() != stickyDate.getMonthOfYear()) {
                            result += "\n" + crtDate.getYear();
                        }
                    }
                    else {
                        result = Integer.toString(crtDay);
                    }
                    break;
                case WEEKS:
                    LocalDate firstMondayOfThisMonth = crtDate.dayOfMonth().withMinimumValue();
                    while (firstMondayOfThisMonth.getDayOfWeek() != DateTimeConstants.MONDAY ) {
                        firstMondayOfThisMonth = firstMondayOfThisMonth.plusDays(1);
                    }
                    final int firstMondayOfThisMonthIdx = firstMondayOfThisMonth.getDayOfMonth();

                    if(crtDay == firstMondayOfThisMonthIdx) {
                        result = crtDate.toString(monthFormatter);
                        if (crtDate.getMonthOfYear() == 1 && crtDate.getMonthOfYear() != stickyDate.getMonthOfYear()) {
                            result += "\n" + crtDate.getYear();
                        }
                    }
                    else {
                        result = Integer.toString(crtDay);
                    }
                    break;
                case MONTHS:
                    result = crtDate.toString(monthFormatter);
                    if (crtDate.getMonthOfYear() == 1 && crtDate.getMonthOfYear() != stickyDate.getMonthOfYear()) {
                        result += "\n" + crtDate.getYear();
                    }
                    break;
                default:
                    result = "";
                    break;
            }
        }
        return result;
    }

    void setRangeType(SpinnerRangeType rangeType) {
        mRangeType = rangeType;
    }
}