package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.bl.TimeProvider

class FakeTimeProvider: TimeProvider {
    var elapsedRealtime = 0L

    override fun now() = 0L
    override fun elapsedRealtime() = elapsedRealtime
}