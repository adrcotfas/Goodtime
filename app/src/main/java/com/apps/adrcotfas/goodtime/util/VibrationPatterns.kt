package com.apps.adrcotfas.goodtime.util


object VibrationPatterns {
    private val NONE = longArrayOf()
    private val SOFT = longArrayOf(0, 50, 50, 50, 50, 50)
    private val STRONG = longArrayOf(0, 500, 500, 500)
    private const val dot = 150
    private const val dash = 375
    private const val short_gap = 150
    private const val medium_gap = 375
    private val SOS_PATTERN = longArrayOf(
        0,
        dot.toLong(), short_gap.toLong(), dot.toLong(), short_gap.toLong(), dot.toLong(),
        medium_gap.toLong(),
        dash.toLong(), short_gap.toLong(), dash.toLong(), short_gap.toLong(), dash.toLong(),
        medium_gap.toLong(),
        dot.toLong(), short_gap.toLong(), dot.toLong(), short_gap.toLong(), dot
            .toLong()
    )
    private const val beat = 250
    private const val interbeat = 100
    private const val between_beat_pairs = 700
    private val HEARTBEAT_PATTERN = longArrayOf(
        0,
        beat.toLong(), interbeat.toLong(), beat.toLong(),
        between_beat_pairs.toLong(),
        beat.toLong(), interbeat.toLong(), beat.toLong()
    )
    val LIST = arrayOf(NONE, SOFT, STRONG, SOS_PATTERN, HEARTBEAT_PATTERN)
}