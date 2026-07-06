package com.passgo.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = VaultEntity::class,
            parentColumns = ["id"],
            childColumns = ["vault_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vault_id"]),
        Index(value = ["deleted_at"])
    ]
)
data class TagEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "vault_id")
    val vaultId: String,
    val name: String,
    val color: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
    @ColumnInfo(name = "sync_version")
    val syncVersion: Int = 0,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"
)
