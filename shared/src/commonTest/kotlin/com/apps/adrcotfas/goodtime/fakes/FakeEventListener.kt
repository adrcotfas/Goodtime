package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.domain.Event
import com.apps.adrcotfas.goodtime.domain.EventListener
import com.apps.adrcotfas.goodtime.domain.TimerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeEventListener : EventListener {

    var timerData = TimerData(Label())
    private val _labelId = MutableStateFlow<String?>(null)
    val labelId : StateFlow<String?> = _labelId

    override fun onEvent(event: Event) {
        when (event) {
            is Event.StartEvent -> timerData = event.timerData
            is Event.AddOneMinute, Event.PauseEvent, Event.Finished -> {}
            is Event.Reset -> timerData = event.timerData
            is Event.SetLabelName -> _labelId.value = event.labelId
        }
    }
}