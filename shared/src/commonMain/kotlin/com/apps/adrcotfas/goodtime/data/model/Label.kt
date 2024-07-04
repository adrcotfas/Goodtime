package com.apps.adrcotfas.goodtime.data.model

import kotlin.random.Random

data class Label(
    val id: Long = 0,
    val name: String = "",
    val colorIndex: Long = 17,
    val orderIndex: Long = Long.MAX_VALUE,
    val useDefaultTimeProfile: Boolean = true,
    val timerProfile: TimerProfile = TimerProfile(),
    val isArchived: Boolean = false
) {
    companion object {
        const val DEFAULT_LABEL_NAME = "PRODUCTIVITY_DEFAULT_LABEL"
        const val DEFAULT_LABEL_COLOR_INDEX = 17L
        const val LABEL_NAME_MAX_LENGTH = 32
        fun defaultLabel() = Label(name = DEFAULT_LABEL_NAME, colorIndex = DEFAULT_LABEL_COLOR_INDEX, orderIndex = 0)
        fun newLabelWithRandomColorIndex(lastIndex: Int) = Label(colorIndex = Random.nextInt(lastIndex).toLong())
    }

    fun isSameAs(label: Label) : Boolean {
        return this.copy(id = 0, orderIndex = 0) == label.copy(id = 0, orderIndex = 0)
    }
}

fun Label.isDefault() = name == Label.DEFAULT_LABEL_NAME
