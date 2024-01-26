package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.domain.TimeProvider

class FakeTimeProvider: TimeProvider {
    var currentTime = 0L
    override fun now() = currentTime
}