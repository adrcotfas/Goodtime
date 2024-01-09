package com.apps.adrcotfas.goodtime.data.timer

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import app.cash.turbine.test
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepositoryImpl
import com.apps.adrcotfas.goodtime.data.local.testDbConnection
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TimerDataRepositoryTest {

    private val fakeFileSystem = FakeFileSystem()
    private val filePath = "/timer.preferences_pb".toPath()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var timerDataRepo: TimerDataRepository
    private lateinit var localDataRepo: LocalDataRepository

    @BeforeTest
    fun setup() = runTest {
        localDataRepo = LocalDataRepositoryImpl(Database(driver = testDbConnection()))
        localDataRepo.insertLabel(label)
        launch {
            timerDataRepo = TimerDataRepositoryImpl(
                dataStore = PreferenceDataStoreFactory.create(
                    storage = OkioStorage(fakeFileSystem, PreferencesSerializer) {
                        filePath
                    }
                ),
                localDataRepository = localDataRepo,
                coroutineScope = testScope
            )
        }
    }

    @AfterTest
    fun tearDown() = runTest {
        fakeFileSystem.checkNoOpenFiles()
        localDataRepo.deleteAllSessions()
        localDataRepo.deleteAllLabels()
    }

    @Test
    fun `Verify first run for default label and subsequently label changes`() = runTest {
        timerDataRepo.timerData.test {
            assertEquals(
                awaitItem(),
                TimerData(),
                "the initial state should be empty since it's a fresh install"
            )
            val labelId = awaitItem().labelId
            assertNotNull(
                labelId,
                "the default label id was fetched labelId should not be null"
            )
            localDataRepo.selectDefaultLabel().test {
                val defaultLabel = awaitItem()
                // make sure that the default label is the same as the one fetched from the timerDataRepo
                assertEquals(defaultLabel.id, labelId)
                assertNull(defaultLabel.name)
            }
            localDataRepo.updateDefaultLabelTimerProfile(customTimerProfile)
            localDataRepo.selectDefaultLabel().first().let {
                assertEquals(it.timerProfile, customTimerProfile)
            }
            localDataRepo.selectLabelByName(CUSTOM_LABEL_NAME).first().let {
                timerDataRepo.setLabelId(it.id)
                assertEquals(
                    awaitItem().labelId,
                    it.id,
                    "the label should have been updated to the new custom one set above"
                )
                assertEquals(it.timerProfile, label.timerProfile)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private const val CUSTOM_LABEL_NAME = "dummy"
        private val defaultTimerProfile = TimerProfile()
        private val customTimerProfile = TimerProfile(isCountdown = false, workBreakRatio = 42)
        private val label = Label(
            id = 0,
            name = CUSTOM_LABEL_NAME,
            colorIndex = 0,
            orderIndex = 0,
            useDefaultTimeProfile = true,
            timerProfile = defaultTimerProfile,
            isArchived = false
        )
    }
}