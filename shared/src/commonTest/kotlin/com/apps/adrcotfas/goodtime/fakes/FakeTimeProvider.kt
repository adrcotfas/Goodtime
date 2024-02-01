package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.domain.TimeProvider

class FakeTimeProvider: TimeProvider {
    var now = 0L
    var elapsedRealtime = 0L

    override fun now() = now
    override fun elapsedRealtime() = elapsedRealtime
}