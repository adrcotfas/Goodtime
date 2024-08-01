package com.apps.adrcotfas.goodtime.common

inline fun <reified T : Enum<T>> T.prettyName(): String {
    return name.lowercase().replaceFirstChar { it.uppercase() }
}

inline fun <reified T : Enum<T>> prettyNames(): List<String> {
    return enumValues<T>().map { it.prettyName() }
}