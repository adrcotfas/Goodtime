package com.apps.adrcotfas.goodtime.Util;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StringUtils {

    private static final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("EEEE', 'MMMM d', ' yyyy");
    private static final DateTimeFormatter backUpFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH:mm");

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    /**
     * Shortens a value in minutes to a easier to read format of maximum 4 characters
     * @param value the value in minutes to be formatted
     * @return the formatted value
     */
    public static String formatLong(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatLong(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatLong(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String formatMinutes(long minutes) {

        final long days   = minutes / 1440;
        final long hours  = minutes / 60 % 24;
        final long remMin = minutes % 60;

        String result;
        if (minutes != 0) {
            result = (days != 0 ? "" + Long.toString(days) + "d" + "\n" : "")
                    + (hours != 0 ? "" + Long.toString(hours) + "h" : "")
                    + (remMin != 0 ? " " + Long.toString(remMin) + "m" : "");
        } else {
            result = "-";
        }
        return result;
    }

    public static String formatDate(long millis) {
        return monthFormatter.print(millis);
    }

    public static String formatDateAndTime(long millis) {
        return backUpFormatter.print(millis);
    }

    public static String formatWeekRange(LocalDate begin, LocalDate end) {
        String beginMonth = begin.toString("MMM");
        String endMonth = end.toString("MMM");
        return beginMonth + " " + begin.getDayOfMonth() + "–" + (endMonth.equals(beginMonth) ? "" : endMonth + " ") + end.getDayOfMonth();
    }
}
