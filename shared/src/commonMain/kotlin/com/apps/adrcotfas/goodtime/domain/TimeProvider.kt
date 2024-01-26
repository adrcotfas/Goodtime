package com.apps.adrcotfas.goodtime.domain

interface TimeProvider {
    /**
     * Returns the current time in milliseconds since Unix Epoch.
     */
    fun now(): Long
}