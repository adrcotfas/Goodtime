package com.apps.adrcotfas.goodtime.bl

//TODO: find a better name and place for this
object TimeUtils {
    fun Long.formatMilliseconds(): String {
        val totalSeconds = this / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes min $seconds sec"
    }
}