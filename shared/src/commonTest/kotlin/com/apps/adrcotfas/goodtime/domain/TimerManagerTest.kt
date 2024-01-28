package com.apps.adrcotfas.goodtime.domain

import app.cash.turbine.test
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.fakes.FakeEventListener
import com.apps.adrcotfas.goodtime.fakes.FakeLocalDataRepository
import com.apps.adrcotfas.goodtime.fakes.FakeSettingsRepository
import com.apps.adrcotfas.goodtime.fakes.FakeTimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
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

    @BeforeTest
    fun setup() = runTest(testDispatcher) {
        timeProvider.currentTime = 0L
        localDataRepo = FakeLocalDataRepository(listOf(Label(), dummyLabel))
        settingsRepo = FakeSettingsRepository()

        timerManager = TimerManager(
            localDataRepo = localDataRepo,
            settingsRepo = settingsRepo,
            listeners = listOf(fakeEventListener),
            timeProvider,
            coroutineScope = testScope
        )
        timerManager.isReady.first()
    }

    @Test
    fun `Verify first run for default label and subsequently label changes`() = runTest {
        settingsRepo.settings.test { assertEquals(awaitItem().currentTimerData.labelName, null) }
        fakeEventListener.labelId.test { assertEquals(awaitItem(), null) }

        timerManager.setLabelName(CUSTOM_LABEL_NAME)
        settingsRepo.settings.test {
            assertEquals(
                awaitItem().currentTimerData.labelName,
                CUSTOM_LABEL_NAME
            )
        }
        fakeEventListener.labelId.test { assertEquals(awaitItem(), CUSTOM_LABEL_NAME) }

        timerManager.setLabelName(null)
        settingsRepo.settings.test { assertEquals(awaitItem().currentTimerData.labelName, null) }
        fakeEventListener.labelId.test { assertEquals(awaitItem(), null) }
    }

    @Test
    fun `Start then pause and resume a timer`() = runTest {
        timerManager.start(TimerType.WORK)
        val startTime = timeProvider.now()
        val duration = 25.minutes.inWholeMilliseconds
        assertEquals(
            timerManager.timerData.value,
            TimerData(
                label = Label(),
                startTime = startTime,
                endTime = startTime + duration,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                minutesAdded = 0
            )
        )
        val elapsedTime = 1.minutes.inWholeMilliseconds
        timeProvider.currentTime += elapsedTime
        timerManager.pause()
        assertEquals(
            timerManager.timerData.value,
            TimerData(
                label = Label(),
                startTime = startTime,
                endTime = 0,
                tmpRemaining = duration - elapsedTime,
                type = TimerType.WORK,
                state = TimerState.PAUSED,
                minutesAdded = 0
            ),
            "remaining time should be one minute less"
        )
        timeProvider.currentTime += elapsedTime
        val endTime = timerManager.timerData.value.tmpRemaining + timeProvider.currentTime
        timerManager.resume()
        assertEquals(
            timerManager.timerData.value,
            TimerData(
                label = Label(),
                startTime = startTime,
                lastStartTime = timeProvider.currentTime,
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
        val startTime = timeProvider.now()
        val duration = 25.minutes.inWholeMilliseconds
        assertEquals(
            timerManager.timerData.value,
            TimerData(
                label = Label(),
                startTime = startTime,
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
            TimerData(
                label = Label(),
                startTime = startTime,
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
        val startTime = timeProvider.now()
        val duration = 25.minutes.inWholeMilliseconds
        timerManager.finish()
        assertEquals(
            timerManager.timerData.value,
            TimerData(
                label = Label(),
                startTime = startTime,
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
        val endTime = timeProvider.now() + 25.minutes.inWholeMilliseconds
        timerManager.start(TimerType.WORK)
        timerManager.reset()
        assertEquals(
            timerManager.timerData.value,
            TimerData(label = Label()),
            "the timer should have been reset"
        )
        assertEquals(fakeEventListener.timerData.endTime, endTime)
    }

    @Test
    fun `Change label from countdown to count-up while timer is running`() = runTest {
        timerManager.start(TimerType.WORK)
        timerManager.setLabelName(CUSTOM_LABEL_NAME)
        assertEquals(
            timerManager.timerData.value,
            TimerData(
                label = dummyLabel,
                startTime = timeProvider.now(),
                endTime = timeProvider.now() + 25.minutes.inWholeMilliseconds,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                minutesAdded = 0
            )
        )
    }

    companion object {
        private const val CUSTOM_LABEL_NAME = "dummy"
        private val dummyTimerProfile = TimerProfile().copy(isCountdown = false, workBreakRatio = 5)
        private val dummyLabel = Label().copy(
            id = 42,
            name = CUSTOM_LABEL_NAME,
            timerProfile = dummyTimerProfile
        )
    }
}