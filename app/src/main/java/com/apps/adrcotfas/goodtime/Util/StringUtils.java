package com.apps.adrcotfas.goodtime.Util;

import java.util.concurrent.TimeUnit;

public class StringUtils {
    public static String formatMinutes(long minutes) {

        final long hours = TimeUnit.MINUTES.toHours(minutes);
        final long remMin = minutes - TimeUnit.HOURS.toMinutes(hours);

        String result;
        if (minutes != 0) {
            result = (hours != 0 ? " " + Long.toString(hours) + "h" : "")
                    + (remMin != 0 ? " " + Long.toString(remMin) + "'" : "");
        } else {
            result = "-";
        }
        return result;
    }
}
