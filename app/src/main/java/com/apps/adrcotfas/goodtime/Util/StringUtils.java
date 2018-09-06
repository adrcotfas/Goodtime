package com.apps.adrcotfas.goodtime.Util;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StringUtils {

    private static final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("EEE dd MMM YYYY");

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
        return (new LocalDate(new Date(millis))).toString(monthFormatter);
    }
}
