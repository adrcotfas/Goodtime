package com.apps.adrcotfas.goodtime.util


object VibrationPatterns {
    private val NONE = longArrayOf()
    private val SOFT = longArrayOf(0, 50, 50, 50, 50, 50)
    private val STRONG = longArrayOf(0, 500, 500, 500)
    private const val DOT = 150
    private const val DASH = 375
    private const val SHORT_GAP = 150
    private const val MEDIUM_GAP = 375
    private val SOS_PATTERN = longArrayOf(
        0,
        DOT.toLong(), SHORT_GAP.toLong(), DOT.toLong(), SHORT_GAP.toLong(), DOT.toLong(),
        MEDIUM_GAP.toLong(),
        DASH.toLong(), SHORT_GAP.toLong(), DASH.toLong(), SHORT_GAP.toLong(), DASH.toLong(),
        MEDIUM_GAP.toLong(),
        DOT.toLong(), SHORT_GAP.toLong(), DOT.toLong(), SHORT_GAP.toLong(), DOT
            .toLong()
    )
    private const val BEAT = 250
    private const val INTER_BEAT = 100
    private const val BETWEEN_BEAT_PAIRS = 700
    private val HEARTBEAT_PATTERN = longArrayOf(
        0,
        BEAT.toLong(), INTER_BEAT.toLong(), BEAT.toLong(),
        BETWEEN_BEAT_PAIRS.toLong(),
        BEAT.toLong(), INTER_BEAT.toLong(), BEAT.toLong()
    )
    val LIST = arrayOf(NONE, SOFT, STRONG, SOS_PATTERN, HEARTBEAT_PATTERN)
}