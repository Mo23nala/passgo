package com.passgo.app.core.model

data class VaultItem(
    val id: String,
    val vaultId: String,
    val folderId: String? = null,
    val category: VaultItemCategory,
    val name: String,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncVersion: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
