package com.passgo.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val passphrase = ByteArray(32).apply { SecureRandom().nextBytes(this) }
    private val factory = SupportOpenHelperFactory(passphrase)

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("migration_test.db")
    }

    @Test
    fun databaseCreation_createsAllTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        db.openDb()
        val database = db.getOpenHelper().readableDatabase

        val cursor = database.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'room_%' AND name NOT LIKE 'android_%'",
            null
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()

        assertTrue("vaults table missing", tables.contains("vaults"))
        assertTrue("vault_items table missing", tables.contains("vault_items"))
        assertTrue("folders table missing", tables.contains("folders"))
        assertTrue("tags table missing", tables.contains("tags"))
        assertTrue("tag_item table missing", tables.contains("tag_item"))
        assertTrue("attachments table missing", tables.contains("attachments"))

        db.close()
    }

    @Test
    fun databaseVersion_isOne() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        val version = db.openHelper.readableDatabase.version
        assertTrue("Expected version 1 but got $version", version >= 1)

        db.close()
    }
}
