package com.apps.adrcotfas.goodtime.domain

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepositoryImpl
import com.apps.adrcotfas.goodtime.data.local.testDbConnection
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.fakes.FakeEventListener
import com.apps.adrcotfas.goodtime.fakes.FakeSettingsRepository
import com.apps.adrcotfas.goodtime.fakes.FakeTimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class TimerManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var localDataRepo: LocalDataRepository

    private lateinit var timerManager: TimerManager

    private val timeProvider = FakeTimeProvider()
    private val fakeEventListener = FakeEventListener()
    private val logger = Logger(loggerConfigInit(platformLogWriter(NoTagFormatter)))

    @BeforeTest
    fun setup() = runTest(testDispatcher) {
        timeProvider.elapsedRealtime = 0L
        localDataRepo = LocalDataRepositoryImpl(
            Database(driver = testDbConnection()),
            defaultDispatcher = testDispatcher
        )

        localDataRepo.insertLabel(Label())
        defaultLabel = defaultLabel.copy(id = localDataRepo.selectLastInsertLabelId()!!)
        localDataRepo.insertLabel(dummyLabel)
        dummyLabel = dummyLabel.copy(id = localDataRepo.selectLastInsertLabelId()!!)

        settingsRepo = FakeSettingsRepository()

        timerManager = TimerManager(
            localDataRepo = localDataRepo,
            settingsRepo = settingsRepo,
            listeners = listOf(fakeEventListener),
            timeProvider,
            logger
        )
        testScope.launch { timerManager.init() }
        timerManager.timerData.first { it.label != null }
    }

    @Test
    fun `Verify first run for default label and subsequently label changes`() = runTest {
        assertEquals(timerManager.timerData.value.label, defaultLabel)
        val currentLabel = settingsRepo.settings.stateIn(testScope).value.persistedTimerData

        settingsRepo.savePersistedTimerData(currentLabel.copy(labelName = CUSTOM_LABEL_NAME))
        assertEquals(timerManager.timerData.value.label, dummyLabel)
        assertEquals(
            timerManager.timerData.value.persistedTimerData,
            currentLabel.copy(labelName = CUSTOM_LABEL_NAME)
        )

        settingsRepo.savePersistedTimerData(currentLabel.copy(labelName = null))
        assertEquals(timerManager.timerData.value.label, defaultLabel)
        assertEquals(
            timerManager.timerData.value.persistedTimerData,
            currentLabel.copy(labelName = null)
        )

        val newTimerProfile = TimerProfile().copy(isCountdown = false, workBreakRatio = 42)
        localDataRepo.updateDefaultLabelTimerProfile(newTimerProfile)
        assertEquals(
            timerManager.timerData.value.label!!.timerProfile,
            newTimerProfile,
            "Modifying the label did not trigger an update"
        )
    }

    @Test
    fun `Start then pause and resume a timer`() = runTest {
        timerManager.start(TimerType.WORK)
        val startTime = timeProvider.elapsedRealtime()
        val duration = 25.minutes.inWholeMilliseconds
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = startTime,
                endTime = startTime + duration,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                minutesAdded = 0
            )
        )
        val elapsedTime = 1.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime += elapsedTime
        timerManager.pause()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = startTime,
                endTime = startTime + duration,
                tmpRemaining = duration - elapsedTime,
                type = TimerType.WORK,
                state = TimerState.PAUSED,
                minutesAdded = 0
            ),
            "remaining time should be one minute less"
        )
        timeProvider.elapsedRealtime += elapsedTime
        val endTime = timerManager.timerData.value.tmpRemaining + timeProvider.elapsedRealtime
        timerManager.resume()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = timeProvider.elapsedRealtime,
                endTime = endTime,
                tmpRemaining = 0,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                minutesAdded = 0
            ),
            "the timer should end after 2 more minutes"
        )
    }

    @Test
    fun `Add one minute`() = runTest {
        timerManager.start(TimerType.WORK)
        val startTime = timeProvider.elapsedRealtime()
        val duration = 25.minutes.inWholeMilliseconds
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = startTime,
                endTime = startTime + duration,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                minutesAdded = 0
            ),
            "the timer should have started"
        )
        timerManager.addOneMinute()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = startTime,
                endTime = startTime + duration + 1.minutes.inWholeMilliseconds,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                minutesAdded = 1
            ),
            "the timer should have been prolonged by one minute"
        )
    }

    @Test
    fun `Timer finish`() = runTest {
        timerManager.start(TimerType.WORK)
        val startTime = timeProvider.elapsedRealtime()
        val duration = 25.minutes.inWholeMilliseconds
        timerManager.finish()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = startTime,
                endTime = startTime + duration,
                type = TimerType.WORK,
                state = TimerState.FINISHED,
                minutesAdded = 0
            ),
            "the timer should have finished"
        )
    }

    @Test
    fun `Timer reset`() = runTest {
        timerManager.start(TimerType.WORK)
        timerManager.reset()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(label = defaultLabel),
            "the timer should have been reset"
        )
        assertEquals(fakeEventListener.events, listOf(Event.Start, Event.Reset))
    }

    @Test
    fun `Change label from countdown to count-up while timer is running`() = runTest {
        timerManager.start(TimerType.WORK)
        val timerData = settingsRepo.settings.stateIn(testScope).value.persistedTimerData
        settingsRepo.savePersistedTimerData(timerData.copy(labelName = CUSTOM_LABEL_NAME))
        assertEquals(timerManager.timerData.value.label, dummyLabel)
    }

    companion object {
        private const val CUSTOM_LABEL_NAME = "dummy"
        private val dummyTimerProfile = TimerProfile().copy(isCountdown = false, workBreakRatio = 5)

        private var defaultLabel = Label()
        private var dummyLabel =
            Label().copy(name = CUSTOM_LABEL_NAME, timerProfile = dummyTimerProfile)
    }
}