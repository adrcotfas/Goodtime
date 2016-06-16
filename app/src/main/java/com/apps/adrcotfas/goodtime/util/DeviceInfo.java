package com.apps.adrcotfas.goodtime.util;

import android.os.Build;
import android.text.TextUtils;

public class DeviceInfo {
    public static String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = Integer.toString(Build.VERSION.SDK_INT);
        String versionRelease = Build.VERSION.RELEASE;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model + " / API level " + version + ", version " + versionRelease;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
}
