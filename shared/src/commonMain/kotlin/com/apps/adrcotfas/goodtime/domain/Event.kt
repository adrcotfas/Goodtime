package com.apps.adrcotfas.goodtime.domain

sealed class Event {
    data class StartEvent(val timerData: DomainTimerData) : Event()
    data object AddOneMinute : Event()
    data object PauseEvent : Event()
    data object Finished : Event()
    data class Reset(val timerData: DomainTimerData) : Event()
}

interface EventListener {
    fun onEvent(event: Event)
}