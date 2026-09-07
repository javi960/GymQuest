package com.gymquest.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymquest.app.data.local.entity.GymEntity
import com.gymquest.app.data.local.entity.GymMachineEntity
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MachineDaoTest {
    private lateinit var database: GymQuestDatabase
    private val timestamp = Instant.parse("2026-09-04T10:15:30Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, GymQuestDatabase::class.java)
            .addCallback(DatabaseMigrations.INTEGRITY_CALLBACK)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observesOnlyActiveMachinesForAnActiveGymInStableOrder() = runBlocking {
        val gymId = insertGym("Local gym")
        val archivedGymId = insertGym("Old gym", isArchived = true)
        database.machineDao().insertMachine(machine(gymId, "Z Press"))
        val archivedMachineId = database.machineDao().insertMachine(machine(gymId, "A Archived"))
        database.machineDao().insertMachine(machine(gymId, "A Bench"))
        database.machineDao().insertMachine(machine(archivedGymId, "Hidden machine"))

        database.machineDao().archiveMachine(archivedMachineId, timestamp)

        assertEquals(
            listOf("A Bench", "Z Press"),
            database.machineDao().observeActiveMachinesForGym(gymId).first().map { it.name },
        )
        assertEquals(emptyList<String>(), database.machineDao().observeActiveMachinesForGym(archivedGymId).first().map { it.name })
    }

    @Test
    fun switchesDefaultGymAtomicallyAndKeepsItWhenTargetIsArchived() = runBlocking {
        val firstGymId = insertGym("First gym")
        val secondGymId = insertGym("Second gym")
        val archivedGymId = insertGym("Archived gym", isArchived = true)

        assertEquals(true, database.machineDao().setDefaultGym(firstGymId, timestamp))
        assertEquals(firstGymId, database.machineDao().observeActiveDefaultGym().first()?.id)

        assertEquals(true, database.machineDao().setDefaultGym(secondGymId, timestamp))
        assertEquals(secondGymId, database.machineDao().observeActiveDefaultGym().first()?.id)

        assertFalse(database.machineDao().setDefaultGym(archivedGymId, timestamp))
        assertEquals(secondGymId, database.machineDao().observeActiveDefaultGym().first()?.id)
        assertEquals(emptyList<String>(), database.machineDao().observeActiveMachinesForGym(999L).first().map { it.name })
    }

    private suspend fun insertGym(name: String, isArchived: Boolean = false): Long =
        database.machineDao().insertGym(
            GymEntity(
                name = name,
                isArchived = isArchived,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )

    private fun machine(gymId: Long, name: String) = GymMachineEntity(
        gymId = gymId,
        name = name,
        weightComparisonType = WeightComparisonType.NOT_COMPARABLE_BETWEEN_MACHINES,
        createdAt = timestamp,
        updatedAt = timestamp,
    )
}
