package com.apps.adrcotfas.goodtime.data.model

import com.apps.adrcotfas.goodtime.data.model.Label.Companion.DEFAULT_LABEL_NAME

data class Session(
    val id: Long,
    val startTimestamp: Long, // milliseconds since epoch
    val endTimestamp: Long, // milliseconds since epoch
    val duration: Long, // minutes
    val label: String = DEFAULT_LABEL_NAME,
    val notes: String?,
    val isWork: Boolean,
    val isArchived: Boolean
) {
    companion object {
        fun create(start: Long, end: Long, duration: Long, label: String, isWork: Boolean) =
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