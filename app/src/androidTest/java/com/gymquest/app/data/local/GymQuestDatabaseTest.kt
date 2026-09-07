package com.gymquest.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GymQuestDatabaseTest {
    @Test
    fun opensVersionOneSchemaInAnInMemoryDatabase() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = Room.inMemoryDatabaseBuilder(context, GymQuestDatabase::class.java)
            .addCallback(DatabaseMigrations.INTEGRITY_CALLBACK)
            .build()

        try {
            assertNotNull(database.openHelper.writableDatabase)
        } finally {
            database.close()
        }
    }
}
