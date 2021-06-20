package com.apps.adrcotfas.goodtime.Main

import kotlin.random.Random

data class Direction(val horizontal: Horizontal, val vertical: Vertical) {
    companion object {
        @JvmStatic
        fun random(): Direction {
            val r = Random
            return Direction(
                horizontal = if (r.nextBoolean()) Horizontal.LEFT else Horizontal.RIGHT,
                vertical = if (r.nextBoolean()) Vertical.UP else Vertical.DOWN
            )
        }
    }
}

enum class Horizontal { LEFT, RIGHT }
enum class Vertical { UP, DOWN }