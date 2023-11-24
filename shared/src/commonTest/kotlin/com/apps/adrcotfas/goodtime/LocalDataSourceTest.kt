package com.apps.adrcotfas.goodtime

import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.LocalDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocalDataSourceTest {
    private lateinit var dataSource: LocalDataSource

    @BeforeTest
    fun setup() = runTest {
        dataSource = LocalDataSource(Database(driver = testDbConnection()))
        dataSource.deleteAllSessions()
        dataSource.deleteAllLabels()
        dataSource.insertLabel(label)
        dataSource.insertSession(session)
    }

    @Test
    fun `Select all Sessions`() = runTest {
        val sessions = dataSource.selectAllSessions().first()
        assertNotNull(
            sessions.find { it.sameAs(session) },
            "Could not find session"
        )
        val labels = dataSource.selectAllLabels().first()
        assertNotNull(
            labels.find { it == label },
            "Could not find label"
        )
    }

    @Test
    fun `Verify foreign key cascade on label change`() = runTest {
        val session = dataSource.selectAllSessions().first().first()
        dataSource.updateSession(
            session.id, Session(
                id = session.id,
                timestamp = 10,
                duration = 30,
                label = LABEL_NAME,
                notes = null,
                isArchived = false
            )
        )
        assertTrue(
            dataSource.selectAllSessions().first().first().run {
                this.label == LABEL_NAME && !this.isArchived
            }, "Cascade update of Session's label failed after updateSession"
        )

        val newLabelName = "new"
        dataSource.updateLabelName(LABEL_NAME, newLabelName)
        delay(1000)
        assertEquals(
            newLabelName,
            dataSource.selectAllSessions().first().first().label,
            "Cascade update of Session's label failed after updateLabelName"
        )

        dataSource.updateLabelIsArchived(newLabelName, newIsArchived = true)
        assertEquals(
            true,
            dataSource.selectAllSessions().first().first().isArchived,
            "Cascade update of Session's label failed after updateLabelIsArchived"
        )
        dataSource.deleteAllLabels()
        assertTrue(
            dataSource.selectAllSessions().first().first().run {
                this.label == null && !this.isArchived
            }, "Cascade update of Session's label failed after deleteAllLabels"
        )
    }

    @Test
    fun `Delete entities`() = runTest {
        dataSource.deleteAllSessions()
        val sessions = dataSource.selectAllSessions().first()
        assertTrue(sessions.isEmpty(), "deleteAllSessions failed")

        dataSource.deleteAllLabels()
        val labels = dataSource.selectAllLabels().first()
        assertTrue(labels.isEmpty(), "selectAllLabels failed")
    }

    companion object {
        private fun Session.sameAs(other: Session): Boolean {
            return timestamp == other.timestamp &&
                    duration == other.duration &&
                    label == other.label &&
                    notes == other.notes &&
                    isArchived == other.isArchived
        }

        private const val LABEL_NAME = "label_name"
        private val label = Label(
            name = LABEL_NAME,
            colorIndex = 0,
            orderIndex = 0,
            shouldFollowDefaultTimeProfile = false,
            isCountdown = false,
            workDuration = 0,
            breakDuration = 0,
            longBreakDuration = 0,
            sessionsBeforeLongBreak = 0,
            workBreakRatio = 0,
            isArchived = false
        )
        private val session = Session(
            id = 0,
            timestamp = 0,
            duration = 25,
            label = null,
            notes = null,
            isArchived = false
        )
    }
}