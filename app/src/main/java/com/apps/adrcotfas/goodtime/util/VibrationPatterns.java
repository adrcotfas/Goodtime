package com.apps.adrcotfas.goodtime.util;

public class VibrationPatterns {

    private static final long[] NONE = {};
    private static final long[] SOFT =  {0, 50, 50, 50, 50, 50};
    private static final long[] STRONG =  {0, 500, 500, 500};

    private static final int dot = 150;
    private static final int dash = 375;
    private static final int short_gap = 150;
    private static final int medium_gap = 375;

    private static final long[] SOS_PATTERN = {
            0,
            dot, short_gap, dot, short_gap, dot,
            medium_gap,
            dash, short_gap, dash, short_gap, dash,
            medium_gap,
            dot, short_gap, dot, short_gap, dot
    };

    private static final int beat = 250;
    private static final int interbeat = 100;
    private static final int between_beat_pairs = 700;
    private static final long[] HEARTBEAT_PATTERN = {
            0,
            beat, interbeat, beat,
            between_beat_pairs,
            beat, interbeat, beat,
    };

    public static final long[][] LIST = {NONE, SOFT, STRONG, SOS_PATTERN, HEARTBEAT_PATTERN};
}
