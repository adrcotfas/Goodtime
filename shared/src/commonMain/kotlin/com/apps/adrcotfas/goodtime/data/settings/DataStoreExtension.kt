package com.apps.adrcotfas.goodtime.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> DataStore<Preferences>.add(key: Preferences.Key<String>, value: T) {
    val existingValues = data.map {
        it[key]?.let { json -> Json.decodeFromString<MutableSet<T>>(json) } ?: mutableSetOf()
    }.first()

    existingValues.add(value)
    edit { it[key] = Json.encodeToString(existingValues) }
}

suspend inline fun <reified T> DataStore<Preferences>.remove(key: Preferences.Key<String>, value: T) {
    val existingValues = data.map {
        it[key]?.let { json -> Json.decodeFromString<MutableSet<T>>(json) } ?: mutableSetOf()
    }.first()

    existingValues.remove(value)
    edit { it[key] = Json.encodeToString(existingValues) }
}