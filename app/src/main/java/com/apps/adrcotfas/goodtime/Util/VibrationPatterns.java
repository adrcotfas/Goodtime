package com.apps.adrcotfas.goodtime.Util;

public class VibrationPatterns {

    private static final long[] DEFAULT_PATTERN = {
            0,
            250, 250, 250,
            250, 250, 250,
            250, 250, 250,
            250, 250, 250,
    };

    private static final int dot = 150;          // Length of a Morse Code "dot" in milliseconds
    private static final int dash = 375;         // Length of a Morse Code "dash" in milliseconds
    private static final int short_gap = 150;    // Length of Gap Between dots/dashes
    private static final int medium_gap = 375;   // Length of Gap Between Letters

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

    private static final long[] JACKHAMMER_PATTERN = {
            0,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100, 100,
            100
    };

    public static final long[][] LIST = {DEFAULT_PATTERN, SOS_PATTERN, HEARTBEAT_PATTERN, JACKHAMMER_PATTERN};
}
