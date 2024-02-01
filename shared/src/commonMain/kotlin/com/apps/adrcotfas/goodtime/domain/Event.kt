package com.apps.adrcotfas.goodtime.domain

sealed class Event {
    data object Start : Event()
    data object Pause : Event()
    data object NextSession: Event()
    data object AddOneMinute : Event()
    data object Finished : Event()
    data object Reset : Event()
}

interface EventListener {
    fun onEvent(event: Event)
}