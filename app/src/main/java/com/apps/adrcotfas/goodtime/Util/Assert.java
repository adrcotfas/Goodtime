package com.apps.adrcotfas.goodtime.Util;

public class Assert {
    public static void that(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
