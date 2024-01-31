package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.domain.DomainTimerData
import com.apps.adrcotfas.goodtime.domain.Event
import com.apps.adrcotfas.goodtime.domain.EventListener

class FakeEventListener : EventListener {

    var timerData = DomainTimerData()

    override fun onEvent(event: Event) {
        when (event) {
            is Event.StartEvent -> timerData = event.timerData
            is Event.AddOneMinute, Event.PauseEvent, Event.Finished -> {}
            is Event.Reset -> timerData = event.timerData
        }
    }
}