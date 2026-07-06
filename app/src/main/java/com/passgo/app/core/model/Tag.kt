package com.passgo.app.core.model

data class Tag(
    val id: String,
    val vaultId: String,
    val name: String,
    val color: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncVersion: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
