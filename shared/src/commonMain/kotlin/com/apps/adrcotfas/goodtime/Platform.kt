package com.apps.adrcotfas.goodtime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform