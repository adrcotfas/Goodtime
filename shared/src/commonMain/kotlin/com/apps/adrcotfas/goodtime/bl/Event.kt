package com.apps.adrcotfas.goodtime.bl

sealed class Event {
    data class Start(val autoStarted: Boolean = false, val endTime: Long) : Event()
    data object Pause : Event()
    data class AddOneMinute(val endTime: Long) : Event()
    data class Finished(val type: TimerType, val autostartNextSession: Boolean) : Event()
    data object Reset : Event()
}

interface EventListener {
    fun onEvent(event: Event)
    companion object
}