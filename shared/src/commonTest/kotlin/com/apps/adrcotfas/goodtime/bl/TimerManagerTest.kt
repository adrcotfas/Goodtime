package com.apps.adrcotfas.goodtime.bl

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepositoryImpl
import com.apps.adrcotfas.goodtime.data.local.testDbConnection
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimerManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var localDataRepo: LocalDataRepository

    private lateinit var timerManager: TimerManager

    private val timeProvider = FakeTimeProvider()
    private val fakeEventListener = FakeEventListener()
    private val logger = Logger(StaticConfig())

    private lateinit var finishedSessionsHandler: FinishedSessionsHandler
    private lateinit var streakAndLongBreakHandler: StreakAndLongBreakHandler

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

        finishedSessionsHandler = FinishedSessionsHandler(
            coroutineScope = testScope,
            repo = localDataRepo,
            log = logger
        )

        streakAndLongBreakHandler = StreakAndLongBreakHandler(
            coroutineScope = testScope,
            settingsRepo = settingsRepo,
        )

        timerManager = TimerManager(
            localDataRepo = localDataRepo,
            settingsRepo = settingsRepo,
            listeners = listOf(fakeEventListener),
            timeProvider,
            finishedSessionsHandler,
            streakAndLongBreakHandler,
            logger
        )
        testScope.launch { timerManager.init() }
        timerManager.timerData.first { it.label != null }
    }

    @Test
    fun `Verify first run for default label and subsequently label changes`() = runTest {
        assertEquals(timerManager.timerData.value.label, defaultLabel)

        settingsRepo.saveLabelName(CUSTOM_LABEL_NAME)
        assertEquals(timerManager.timerData.value.label, dummyLabel)
        assertEquals(
            timerManager.timerData.value.label!!.name,
            CUSTOM_LABEL_NAME
        )

        settingsRepo.saveLabelName(null)
        assertEquals(timerManager.timerData.value.label, defaultLabel)
        assertEquals(
            timerManager.timerData.value.label!!.name,
            null
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
    fun `Init persistent data only once`() = runTest {
        val customLongBreakData = LongBreakData(10, 42)
        settingsRepo.saveLongBreakData(customLongBreakData)
        val customBreakBudgetData = BreakBudgetData(10, 42)
        settingsRepo.saveBreakBudgetData(customBreakBudgetData)

        assertNotEquals(timerManager.timerData.value.longBreakData, customLongBreakData)
        assertNotEquals(timerManager.timerData.value.breakBudgetData, customBreakBudgetData)
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
                remainingTimeAtPause = duration - elapsedTime,
                type = TimerType.WORK,
                state = TimerState.PAUSED,
            ),
            "remaining time should be one minute less"
        )
        timeProvider.elapsedRealtime += elapsedTime
        val endTime =
            timerManager.timerData.value.remainingTimeAtPause + timeProvider.elapsedRealtime
        timerManager.resume()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                startTime = startTime,
                lastStartTime = timeProvider.elapsedRealtime,
                endTime = endTime,
                remainingTimeAtPause = 0,
                type = TimerType.WORK,
                state = TimerState.RUNNING,
                pausedTime = 1.minutes.inWholeMilliseconds
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
            ),
            "the timer should have been prolonged by one minute"
        )
    }

    @Test
    fun `Skip session after one minute`() = runTest {
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
            ),
            "the timer should have started"
        )
        val elapsedTime = 1.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime += elapsedTime
        timeProvider.now += elapsedTime
        timerManager.next()
        assertEquals(timerManager.timerData.value.state, TimerState.RUNNING)
        assertEquals(timerManager.timerData.value.type, TimerType.BREAK)
        localDataRepo.selectSessionById(localDataRepo.selectLastInsertSessionId()!!).test {
            val session = awaitItem()
            assertEquals(session.duration.minutes.inWholeMilliseconds, elapsedTime)
            assertEquals(session.startTimestamp, 0)
            assertEquals(session.endTimestamp, elapsedTime)
            assertEquals(session.isWork, true)
        }
        timeProvider.elapsedRealtime += elapsedTime
        timeProvider.now += elapsedTime
        timerManager.next()
        assertEquals(timerManager.timerData.value.state, TimerState.RUNNING)
        assertEquals(timerManager.timerData.value.type, TimerType.WORK)
        localDataRepo.selectSessionById(localDataRepo.selectLastInsertSessionId()!!).test {
            val session = awaitItem()
            assertEquals(session.duration.minutes.inWholeMilliseconds, elapsedTime)
            assertEquals(session.startTimestamp, elapsedTime)
            assertEquals(session.endTimestamp, elapsedTime + elapsedTime)
            assertEquals(session.isWork, false)
        }
    }

    @Test
    fun `Skip timer before one minute`() = runTest {
        timerManager.start(TimerType.WORK)
        val endTime = timerManager.timerData.value.endTime
        val duration = 59.seconds.inWholeMilliseconds
        timeProvider.elapsedRealtime = duration
        timeProvider.now = duration
        timerManager.next()
        assertEquals(
            fakeEventListener.events,
            listOf(
                Event.Start(endTime),
                Event.Start(timeProvider.elapsedRealtime + TimerProfile.DEFAULT_BREAK_DURATION.minutes.inWholeMilliseconds)
            )
        )
        localDataRepo.selectAllSessions().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `Timer finish`() = runTest {
        timerManager.start(TimerType.WORK)
        val duration = TimerProfile.DEFAULT_WORK_DURATION.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime = duration
        timeProvider.now = duration
        timerManager.finish()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                longBreakData = LongBreakData(
                    streak = 1,
                    lastWorkEndTime = timeProvider.now
                ),
                startTime = 0,
                lastStartTime = 0,
                endTime = duration,
                type = TimerType.WORK,
                state = TimerState.FINISHED,
            ),
            "the timer should have finished"
        )
        localDataRepo.selectSessionById(localDataRepo.selectLastInsertSessionId()!!).test {
            val session = awaitItem()
            assertEquals(session.duration.minutes.inWholeMilliseconds, duration)
            assertEquals(session.startTimestamp, 0)
            assertEquals(session.endTimestamp, duration)
        }
    }

    @Test
    fun `Timer reset after one minute`() = runTest {
        timerManager.start(TimerType.WORK)
        val endTime = timerManager.timerData.value.endTime
        val oneMinute = 1.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime = oneMinute
        timeProvider.now = oneMinute
        timerManager.reset()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                longBreakData = LongBreakData(
                    streak = 1,
                    lastWorkEndTime = timeProvider.now
                )
            ),
            "the timer should have been reset"
        )
        assertEquals(
            fakeEventListener.events,
            listOf(Event.Start(endTime), Event.Reset)
        )
        localDataRepo.selectSessionById(localDataRepo.selectLastInsertSessionId()!!).test {
            val session = awaitItem()
            assertEquals(session.duration.minutes.inWholeMilliseconds, oneMinute)
            assertEquals(session.startTimestamp, 0)
            assertEquals(session.endTimestamp, oneMinute)
        }
    }

    @Test
    fun `Timer reset before one minute`() = runTest {
        timerManager.start(TimerType.WORK)
        val endTime = timerManager.timerData.value.endTime
        val duration = 59.seconds.inWholeMilliseconds
        timeProvider.elapsedRealtime = duration
        timeProvider.now = duration
        timerManager.reset()
        assertEquals(
            timerManager.timerData.value,
            DomainTimerData(
                label = defaultLabel,
                longBreakData = LongBreakData(
                    streak = 1,
                    lastWorkEndTime = timeProvider.now
                )
            ),
            "the timer should have been reset"
        )
        assertEquals(
            fakeEventListener.events,
            listOf(Event.Start(endTime), Event.Reset)
        )
        localDataRepo.selectAllSessions().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `Streak increments when interrupting a work session`() = runTest {
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += 1
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        timeProvider.elapsedRealtime += 1
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += 1
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 2)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += 1
        timerManager.finish()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 3)
    }

    @Test
    fun `Pausing for a long time is not considered idle time for streak`() = runTest {
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += 1
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        timerManager.start(TimerType.WORK)
        timerManager.pause()
        val twoHours = 2.hours.inWholeMilliseconds
        timeProvider.elapsedRealtime += twoHours
        timerManager.resume()
        timerManager.finish()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 2)
    }


    @Test
    fun `Long break after 4 work sessions with finish and next`() = runTest {
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
        timerManager.start(TimerType.WORK)
        val twoMinutes = 2.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.finish()
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        assertEquals(timerManager.timerData.value.type, TimerType.BREAK)
        timerManager.next()
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.finish()
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 2)
        assertEquals(timerManager.timerData.value.type, TimerType.BREAK)
        timerManager.next()
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.finish()
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 3)
        assertEquals(timerManager.timerData.value.type, TimerType.BREAK)
        timerManager.next()
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.finish()
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 4)
        assertEquals(timerManager.timerData.value.type, TimerType.LONG_BREAK)
    }

    @Test
    fun `Long break after 4 work sessions with reset and next for the last`() = runTest {
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
        timerManager.start(TimerType.WORK)
        val twoMinutes = 2.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 2)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 3)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 4)
        assertEquals(timerManager.timerData.value.type, TimerType.LONG_BREAK)
    }

    @Test
    fun `No long break if idled`() = runTest {
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
        timerManager.start(TimerType.WORK)
        val twoMinutes = 2.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 2)
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += twoMinutes
        timeProvider.now += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 3)
        val oneHour = 1.hours.inWholeMilliseconds
        timeProvider.elapsedRealtime += oneHour
        timerManager.start(TimerType.WORK)
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.next()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        assertEquals(timerManager.timerData.value.type, TimerType.BREAK)
    }

    @Test
    fun `Reset streak after idle`() = runTest {
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
        timerManager.start(TimerType.WORK)
        val twoMinutes = 2.minutes.inWholeMilliseconds
        timeProvider.elapsedRealtime += twoMinutes
        timerManager.reset()
        assertEquals(timerManager.timerData.value.longBreakData.streak, 1)
        val oneHour = 1.hours.inWholeMilliseconds
        timeProvider.elapsedRealtime += oneHour
        timerManager.start(TimerType.WORK)
        assertEquals(timerManager.timerData.value.longBreakData.streak, 0)
    }

    companion object {
        private const val CUSTOM_LABEL_NAME = "dummy"
        private val dummyTimerProfile = TimerProfile().copy(isCountdown = false, workBreakRatio = 5)

        private var defaultLabel = Label()
        private var dummyLabel =
            Label().copy(name = CUSTOM_LABEL_NAME, timerProfile = dummyTimerProfile)
    }
}