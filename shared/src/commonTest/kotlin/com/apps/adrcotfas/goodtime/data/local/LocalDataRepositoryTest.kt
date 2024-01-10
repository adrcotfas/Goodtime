package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocalDataRepositoryTest {
    private lateinit var dataSource: LocalDataRepository

    @BeforeTest
    fun setup() = runTest {
        dataSource = LocalDataRepositoryImpl(Database(driver = testDbConnection()))
        dataSource.deleteAllSessions()
        dataSource.deleteAllLabels()
        dataSource.insertLabel(label)
        dataSource.insertSession(session)
    }

    @Test
    fun `Select by name`() = runTest {
        val label = dataSource.selectLabelByName(LABEL_NAME).first()
        assertEquals(LABEL_NAME, label.name, "selectLabelByName failed")
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
    fun `Select entities by Label`() = runTest {
        val filteredSessions = dataSource.selectSessionsByLabel(LABEL_NAME).first()
        assertEquals(0, filteredSessions.size, "There should be no sessions with this label")

        val allSessions = dataSource.selectAllSessions().first()
        dataSource.updateSession(
            allSessions.first().id,
            allSessions.first().copy(label = LABEL_NAME)
        )
        assertEquals(
            1,
            dataSource.selectSessionsByLabel(LABEL_NAME).first().size,
            "updateSession failed"
        )
    }

    @Test
    fun `Select session after timestamp`() = runTest {
        val sessions = dataSource.selectAllSessions().first()
        dataSource.updateSession(sessions.first().id, sessions.first().copy(timestamp = 100))
        assertEquals(1, dataSource.selectAllSessions().first().size, "There should be one session")
        dataSource.deleteSessionAfter(150)
        assertEquals(
            1,
            dataSource.selectAllSessions().first().size,
            "deleteSessionAfter failed; There should be one session"
        )
        dataSource.deleteSessionAfter(99)
        assertEquals(0, dataSource.selectAllSessions().first().size, "deleteSessionAfter failed")

        dataSource.insertSession(session)
        dataSource.updateSession(
            dataSource.selectAllSessions().first().first().id,
            session.copy(timestamp = 100)
        )
        assertEquals(1, dataSource.selectAllSessions().first().size, "There should be one session")
        dataSource.deleteSessionAfter(100)
        assertEquals(0, dataSource.selectAllSessions().first().size, "deleteSessionAfter failed")
    }

    @Test
    fun `Select entities by IsArchived`() = runTest {
        val expectedArchivedSessions = 3
        repeat(expectedArchivedSessions) {
            dataSource.insertSession(session.copy(isArchived = true))
        }

        val sessions = dataSource.selectByIsArchived(true).first()
        assertEquals(expectedArchivedSessions, sessions.size, "selectSessionsByIsArchived failed")

        dataSource.insertLabel(label.copy(name = "ceva", isArchived = true))
        dataSource.insertLabel(label.copy(name = "fin", isArchived = true))

        val labels = dataSource.selectLabelsByArchived(true).first()
        assertEquals(2, labels.size, "selectLabelsByArchived failed")
    }

    @Test
    fun `Select last insert session autoincrement id`() = runTest {
        val firstId = dataSource.selectLastInsertSessionId()
        assertNotNull(firstId, "selectLastInsertSessionId failed")
        dataSource.insertSession(session)
        val secondId = dataSource.selectLastInsertSessionId()
        dataSource.insertSession(session)
        val thirdId = dataSource.selectLastInsertSessionId()
        assertTrue(
            thirdId == secondId!! + 1 && secondId == firstId + 1,
            "selectLastInsertSessionId autoincrement failed"
        )
    }

    @Test
    fun `Update label properties`() = runTest {
        val expectedColorIndex = 9L
        val expectedOrderIndex = 10L
        dataSource.updateLabelColorIndex(LABEL_NAME, expectedColorIndex)
        dataSource.updateLabelOrderIndex(LABEL_NAME, expectedOrderIndex)
        dataSource.updateShouldFollowDefaultTimeProfile(LABEL_NAME, true)

        val label = dataSource.selectAllLabels().first().first()
        assertEquals(expectedColorIndex, label.colorIndex, "updateLabelColorIndex failed")
        assertEquals(expectedOrderIndex, label.orderIndex, "updateLabelOrderIndex failed")
        assertEquals(
            true,
            label.useDefaultTimeProfile,
            "updateShouldFollowDefaultTimeProfile failed"
        )
    }

    @Test
    fun `Delete entities`() = runTest {
        dataSource.deleteAllSessions()
        val sessions = dataSource.selectAllSessions().first()
        assertTrue(sessions.isEmpty(), "deleteAllSessions failed")

        dataSource.insertSession(session)
        assertEquals(1, dataSource.selectAllSessions().first().size, "insertSession failed")

        val sessionId = dataSource.selectAllSessions().first().first().id
        dataSource.deleteSession(sessionId)
        assertEquals(0, dataSource.selectAllSessions().first().size, "deleteSession failed")

        dataSource.deleteAllLabels()
        val labels = dataSource.selectAllLabels().first()
        assertTrue(labels.isEmpty(), "selectAllLabels failed")

        val labelToDeleteName = "ceva"
        dataSource.insertLabel(label.copy(name = labelToDeleteName))
        assertEquals(1, dataSource.selectAllLabels().first().size, "insertLabel failed")
        dataSource.deleteLabel(labelToDeleteName)
        assertEquals(0, dataSource.selectAllLabels().first().size, "deleteLabel failed")
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
            id = 0,
            name = LABEL_NAME,
            colorIndex = 0,
            orderIndex = 0,
            useDefaultTimeProfile = false,
            timerProfile = TimerProfile(),
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