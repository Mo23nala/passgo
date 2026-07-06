package com.passgo.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = Migration(1, 2) { db ->
        db.execSQL("ALTER TABLE vault_items ADD COLUMN username TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vault_items ADD COLUMN password TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vault_items ADD COLUMN url TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE vault_items ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
}
