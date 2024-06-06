package com.apps.adrcotfas.goodtime.data.model

data class Label(
    val id: Long = 0,
    val name: String,
    val colorIndex: Long = 0,
    val orderIndex: Long = Long.MAX_VALUE,
    val useDefaultTimeProfile: Boolean = true,
    val timerProfile: TimerProfile = TimerProfile(),
    val isArchived: Boolean= false
) {
    companion object {
        const val DEFAULT_LABEL_NAME = ""
    }
}


fun Label.isDefault() = name == Label.DEFAULT_LABEL_NAME
