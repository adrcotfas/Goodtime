/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StringUtils {

    private static final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("EEE', 'MMM d', ' yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
    private static final DateTimeFormatter backUpFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH:mm");
    private static final DateTimeFormatter dayOfWeekFormatter = DateTimeFormat.forPattern("E");
    private static final DateTimeFormatter hourOfDayFormatter = DateTimeFormat.forPattern("hh");
    private static final DateTimeFormatter meridianFormatter = DateTimeFormat.forPattern("a");

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
     * Do not use this for anything else than the History chart before removing the extra left padding hack
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
            result = "0m";
        }
        return result;
    }

    public static String formatDate(long millis) {
        return monthFormatter.print(millis);
    }

    public static String formatTime(long millis) {
        return timeFormatter.print(millis);
    }

    public static String formatDateAndTime(long millis) {
        return backUpFormatter.print(millis);
    }

    public static String toPercentage(float value) {
        return Math.round(100 * value) + "%";
    }

    public static String toDayOfWeek(int value) {
        return dayOfWeekFormatter.withLocale(Locale.getDefault()).print(new DateTime().withDayOfWeek(value));
    }

    public static String toHourOfDay(int value) {
        DateTime date = new DateTime().withHourOfDay(value);
        return hourOfDayFormatter.print(date) + "\n" + meridianFormatter.print(date);
    }
}
