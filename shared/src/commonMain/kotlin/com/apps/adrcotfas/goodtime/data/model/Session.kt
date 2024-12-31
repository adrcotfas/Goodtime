package com.apps.adrcotfas.goodtime.data.model

import com.apps.adrcotfas.goodtime.data.model.Label.Companion.DEFAULT_LABEL_NAME

data class Session(
    val id: Long,
    val timestamp: Long, // milliseconds since epoch
    val duration: Long, // minutes
    val interruptions: Long, // minutes
    val label: String = DEFAULT_LABEL_NAME,
    val notes: String?,
    val isWork: Boolean, //TODO: update repository to consider this in stats
    val isArchived: Boolean
) {
    companion object {
        fun create(
            timestamp: Long,
            duration: Long,
            interruptions: Long,
            label: String,
            isWork: Boolean
        ) =
            Session(
                id = 0,
                timestamp = timestamp,
                duration = duration,
                interruptions = interruptions,
                label = label,
                notes = null,
                isWork = isWork,
                isArchived = false
            )
    }
}