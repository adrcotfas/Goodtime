package com.apps.adrcotfas.goodtime.bl

sealed class Event {
    data class Start(val endTime: Long) : Event()
    data object Pause : Event()
    data class NextSession(val endTime: Long): Event()
    data class AddOneMinute(val endTime: Long) : Event()
    data object Finished : Event()
    data object Reset : Event()
}

interface EventListener {
    fun onEvent(event: Event)
    companion object
}