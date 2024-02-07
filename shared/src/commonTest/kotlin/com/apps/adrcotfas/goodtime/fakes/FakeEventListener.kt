package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.bl.Event
import com.apps.adrcotfas.goodtime.bl.EventListener

class FakeEventListener : EventListener {

    private val _events = mutableListOf<Event>()
    val events: List<Event> = _events

    override fun onEvent(event: Event) {
        _events.add(event)
    }
}