package com.apps.adrcotfas.goodtime.Util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.TimeUnit;

public class StringUtils {

    private static final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("EEEE', 'MMMM d', ' yyyy");
    private static final DateTimeFormatter backUpFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH:mm");

    public static String formatMinutes(long minutes) {

        final long hours = TimeUnit.MINUTES.toHours(minutes);
        final long remMin = minutes - TimeUnit.HOURS.toMinutes(hours);

        String result;
        if (minutes != 0) {
            result = (hours != 0 ? " " + Long.toString(hours) + "h" : "")
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
}
