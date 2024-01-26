package com.apps.adrcotfas.goodtime.domain

import kotlinx.datetime.Clock

class TimeProviderImpl: TimeProvider {
    override fun now(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}