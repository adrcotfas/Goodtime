package com.apps.adrcotfas.goodtime.data.model

data class Session(
    val id: Long,
    val startTimestamp: Long, // milliseconds since epoch
    val endTimestamp: Long, // milliseconds since epoch
    val duration: Long, // minutes
    val label: String?,
    val notes: String?,
    val isWork: Boolean,
    val isArchived: Boolean
) {
    companion object {
        fun create(start: Long, end: Long, duration: Long, label: String?, isWork: Boolean) =
            Session(
                id = 0,
                startTimestamp = start,
                endTimestamp = end,
                duration = duration,
                label = label,
                notes = null,
                isWork = isWork,
                isArchived = false
            )
    }
}