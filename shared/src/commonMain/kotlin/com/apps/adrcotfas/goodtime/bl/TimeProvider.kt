package com.apps.adrcotfas.goodtime.bl

interface TimeProvider {
    /**
     * Returns the current time in milliseconds since Unix Epoch.
     */
    fun now(): Long

    /**
     * Returns the current time in milliseconds since boot, including time spent in sleep.
     */
    fun elapsedRealtime(): Long
}