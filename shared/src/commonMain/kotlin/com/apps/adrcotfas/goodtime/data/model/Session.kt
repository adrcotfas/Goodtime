package com.apps.adrcotfas.goodtime.data.model

data class Session(
    val id: Long,
    val timestamp: Long,
    val duration: Long,
    val label: String?,
    val notes: String?,
    val isArchived: Boolean,
)