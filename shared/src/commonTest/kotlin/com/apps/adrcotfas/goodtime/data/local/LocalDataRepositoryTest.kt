/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.model.TimerProfile.Companion.DEFAULT_WORK_DURATION
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

class LocalDataRepositoryTest {
    private lateinit var dataSource: LocalDataRepository

    @BeforeTest
    fun setup() = runTest {
        dataSource = LocalDataRepositoryImpl(Database(driver = testDbConnection()))
        dataSource.deleteAllSessions()
        dataSource.deleteAllLabels()
        dataSource.insertLabel(label)
        dataSource.insertLabel(Label.defaultLabel())
        label = label.copy(id = dataSource.selectLastInsertLabelId()!!)
        dataSource.insertSession(session)
        session = session.copy(id = dataSource.selectLastInsertSessionId()!!)
    }

    @Test
    fun `Select by name`() = runTest {
        val label = dataSource.selectLabelByName(LABEL_NAME).first()
        assertEquals(LABEL_NAME, label!!.name, "selectLabelByName failed")
    }

    @Test
    fun `Select all Sessions`() = runTest {
        val sessions = dataSource.selectAllSessions().first()
        assertNotNull(
            sessions.find { it == session },
            "Could not find session",
        )
        val labels = dataSource.selectAllLabels().first()
        assertNotNull(
            labels.find { it == label },
            "Could not find label",
        )
    }

    @Test
    fun `Verify foreign key cascade on label change`() = runTest {
        val session = dataSource.selectAllSessions().first().first()
        dataSource.updateSession(
            session.id,
            Session(
                id = session.id,
                timestamp = 1.minutes.inWholeMilliseconds,
                duration = 1.minutes.toLong(DurationUnit.MINUTES),
                interruptions = 0,
                label = LABEL_NAME,
                notes = null,
                isWork = true,
                isArchived = false,
            ),
        )
        assertTrue(
            dataSource.selectAllSessions().first().first().run {
                this.label == LABEL_NAME && !this.isArchived
            },
            "Cascade update of Session's label failed after updateSession",
        )

        val newLabelName = "new"
        dataSource.updateLabel(LABEL_NAME, label.copy(name = newLabelName))
        assertEquals(
            newLabelName,
            dataSource.selectAllSessions().first().first().label,
            "Cascade update of Session's label failed after updateLabelName",
        )

        dataSource.updateLabelIsArchived(newLabelName, newIsArchived = true)
        assertEquals(
            true,
            dataSource.selectAllSessions().first().first().isArchived,
            "Cascade update of Session's label failed after updateLabelIsArchived",
        )
        dataSource.deleteAllLabels()
        assertTrue(
            dataSource.selectAllSessions().first().first().run {
                this.label == Label.DEFAULT_LABEL_NAME && !this.isArchived
            },
            "Cascade update of Session's label failed after deleteAllLabels",
        )
    }

    @Test
    fun `Select entities by Label`() = runTest {
        val filteredSessions = dataSource.selectSessionsByLabel(LABEL_NAME).first()
        assertEquals(0, filteredSessions.size, "There should be no sessions with this label")

        val allSessions = dataSource.selectAllSessions().first()
        dataSource.updateSession(
            allSessions.first().id,
            allSessions.first().copy(label = LABEL_NAME),
        )
        assertEquals(
            1,
            dataSource.selectSessionsByLabel(LABEL_NAME).first().size,
            "updateSession failed",
        )
    }

    @Test
    fun `Select entities by IsArchived`() = runTest {
        val expectedArchivedSessions = 3
        repeat(expectedArchivedSessions) {
            dataSource.insertSession(session.copy(label = LABEL_NAME))
        }
        dataSource.updateLabelIsArchived(LABEL_NAME, true)

        val sessions = dataSource.selectByIsArchived(true).first()
        assertEquals(expectedArchivedSessions, sessions.size, "selectSessionsByIsArchived failed")

        dataSource.insertLabel(label.copy(name = "ceva", isArchived = true))
        dataSource.insertLabel(label.copy(name = "fin", isArchived = true))

        val labels = dataSource.selectAllLabelsArchived().first()
        assertEquals(3, labels.size, "selectLabelsByArchived failed")
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
            "selectLastInsertSessionId autoincrement failed",
        )
    }

    @Test
    fun `Update label properties`() = runTest {
        val expectedColorIndex = 9L
        val expectedOrderIndex = 10L
        val expectedFollowDefaultTimeProfile = false
        dataSource.updateLabel(
            LABEL_NAME,
            label.copy(
                colorIndex = expectedColorIndex,
                orderIndex = expectedOrderIndex,
                useDefaultTimeProfile = expectedFollowDefaultTimeProfile,
            ),
        )
        dataSource.updateLabelOrderIndex(LABEL_NAME, expectedOrderIndex)

        val labels = dataSource.selectAllLabels().first()
        val label = labels.firstOrNull { it.name == LABEL_NAME }
        assertNotNull(label, "label not found")
        assertEquals(expectedColorIndex, label.colorIndex, "updateLabelColorIndex failed")
        assertEquals(expectedOrderIndex, label.orderIndex, "updateLabelOrderIndex failed")
        assertEquals(
            expectedFollowDefaultTimeProfile,
            label.useDefaultTimeProfile,
            "updateShouldFollowDefaultTimeProfile failed",
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
        assertNotNull(
            labels.firstOrNull { it.name == Label.DEFAULT_LABEL_NAME },
            "default label should always be present",
        )

        val labelToDeleteName = "ceva"
        dataSource.insertLabel(label.copy(name = labelToDeleteName))
        assertEquals(2, dataSource.selectAllLabels().first().size, "insertLabel failed")
        dataSource.deleteLabel(labelToDeleteName)
        assertEquals(1, dataSource.selectAllLabels().first().size, "deleteLabel failed")
    }

    companion object {
        private const val LABEL_NAME = "label_name"
        private var label = Label(
            id = 0,
            name = LABEL_NAME,
            colorIndex = 0,
            orderIndex = 0,
            useDefaultTimeProfile = false,
            timerProfile = TimerProfile(),
            isArchived = false,
        )

        private val DEFAULT_DURATION = DEFAULT_WORK_DURATION.minutes.inWholeMilliseconds

        private var session = Session(
            id = 0,
            timestamp = DEFAULT_DURATION,
            duration = 25,
            interruptions = 0,
            label = Label.DEFAULT_LABEL_NAME,
            notes = null,
            isWork = true,
            isArchived = false,
        )
    }
}
