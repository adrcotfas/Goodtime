package com.apps.adrcotfas.goodtime.data.timer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimerDataRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    localDataRepository: LocalDataRepository,
    coroutineScope: CoroutineScope
) : TimerDataRepository {

    private object Keys {
        val startTimeKey = longPreferencesKey("startTimeKey")
        val lastStartTimeKey = longPreferencesKey("lastStartTimeKey")
        val labelKey = longPreferencesKey("labelKey")
    }

    override val timerData: Flow<TimerData> = dataStore.data.map {
        TimerData(
            startTime = it[Keys.startTimeKey] ?: 0,
            lastStartTime = it[Keys.lastStartTimeKey] ?: 0,
            labelId = it[Keys.labelKey],
        )
    }.distinctUntilChanged()

    private lateinit var currentLabel: Label
    private lateinit var defaultLabel: Label

    init {
        coroutineScope.launch {
            localDataRepository.selectDefaultLabel().distinctUntilChanged().collect { label ->
                dataStore.edit {
                    defaultLabel = label
                    // this will execute on the first ever launch;
                    // we need to set the default label id which is fetched from the database
                    if (it[Keys.labelKey] == null) {
                        it[Keys.labelKey] = label.id
                        currentLabel = label
                    }
                }
            }
        }
        // when the label id changes, fetch the new label from the database
        coroutineScope.launch {
            dataStore.data.map {
                it[Keys.labelKey]
            }.filterNotNull().distinctUntilChanged().flatMapLatest {
                localDataRepository.selectLabelById(it)
            }.distinctUntilChanged().collect {
                currentLabel = it
            }
        }
    }

    override suspend fun start() {
    }

    override suspend fun addOneMinute() {
        TODO("Not yet implemented")
    }

    override suspend fun pause() {
        TODO("Not yet implemented")
    }

    override suspend fun resume() {
        TODO("Not yet implemented")
    }

    override suspend fun skip() {
        TODO("Not yet implemented")
    }

    override suspend fun reset() {
        TODO("Not yet implemented")
    }

    override suspend fun finish() {
        TODO("Not yet implemented")
    }

    override suspend fun setLabelId(labelId: Long) {
        dataStore.edit {
            it[Keys.labelKey] = labelId
        }
    }
}