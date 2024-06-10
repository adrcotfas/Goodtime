package com.apps.adrcotfas.goodtime.utils

fun generateUniqueNameForDuplicate(name: String, existingNames: List<String>): String {
    return generateUniqueNameForDuplicate(name, existingNames, null)
}

private tailrec fun generateUniqueNameForDuplicate(
    name: String,
    existingNames: List<String>,
    suffix: Int? = null
): String {
    val tmpSuffix = suffix ?: suffix ?: name.getNumberSuffix()
    val newName = tmpSuffix?.let {
        name.dropLast(tmpSuffix.toString().length) + (tmpSuffix + 1)
    } ?: (name + "2")
    return if (existingNames.any { it == newName }) {
        generateUniqueNameForDuplicate(newName, existingNames, tmpSuffix?.let { it + 1 } ?: 2)
    } else {
        newName
    }
}

private fun String.getNumberSuffix(): Int? {
    val matchResult = "\\d+$".toRegex().find(this)
    return matchResult?.value?.toInt()
}