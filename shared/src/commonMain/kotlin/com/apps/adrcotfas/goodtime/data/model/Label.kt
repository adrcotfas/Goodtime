package com.apps.adrcotfas.goodtime.data.model

data class Label(
    val id: Long,
    val name: String?,
    val colorIndex: Long,
    val orderIndex: Long,
    val useDefaultTimeProfile: Boolean,
    val timerProfile: TimerProfile,
    val isArchived: Boolean
)

