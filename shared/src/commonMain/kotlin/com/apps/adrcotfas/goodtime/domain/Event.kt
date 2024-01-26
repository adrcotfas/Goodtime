package com.apps.adrcotfas.goodtime.domain

sealed class Event {
    data class StartEvent(val timerData: TimerData) : Event()
    data object AddOneMinute : Event()
    data object PauseEvent : Event()
    data object Finished : Event()
    data class Reset(val timerData: TimerData) : Event()
    data class SetLabelName(val labelId: String?) : Event()
}

interface EventListener {
    fun onEvent(event: Event)
}