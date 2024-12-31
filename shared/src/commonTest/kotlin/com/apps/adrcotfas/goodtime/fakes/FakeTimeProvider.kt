package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.bl.TimeProvider

class FakeTimeProvider: TimeProvider {
    var elapsedRealtime = 0L

    override fun now() = elapsedRealtime
    override fun elapsedRealtime() = elapsedRealtime
}