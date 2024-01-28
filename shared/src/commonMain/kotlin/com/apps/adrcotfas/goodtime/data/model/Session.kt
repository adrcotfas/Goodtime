package com.apps.adrcotfas.goodtime.data.model

data class Session(
    val id: Long,
    val startTimestamp: Long, // milliseconds since epoch
    val endTimestamp: Long, // milliseconds since epoch
    val duration: Long, // minutes
    val label: String?,
    val notes: String?,
    val isArchived: Boolean,
)