package com.passgo.app.core.model

data class Folder(
    val id: String,
    val vaultId: String,
    val name: String,
    val icon: String = "folder",
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncVersion: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
