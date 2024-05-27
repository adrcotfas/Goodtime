package com.apps.adrcotfas.goodtime.data.model

data class Label(
    val id: Long = 0,
    val name: String = "",
    val colorIndex: Long = 0,
    val orderIndex: Long = 0,
    val useDefaultTimeProfile: Boolean = true,
    val timerProfile: TimerProfile = TimerProfile(),
    val isArchived: Boolean= false
)