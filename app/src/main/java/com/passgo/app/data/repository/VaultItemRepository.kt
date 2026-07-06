package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.ItemType
import com.passgo.app.core.model.VaultItem
import kotlinx.coroutines.flow.Flow

interface VaultItemRepository {
    fun getActiveItems(vaultId: String): Flow<List<VaultItem>>
    fun getByType(vaultId: String, type: ItemType): Flow<List<VaultItem>>
    fun getByFolder(folderId: String): Flow<List<VaultItem>>
    fun getFavorites(vaultId: String): Flow<List<VaultItem>>
    fun getDeleted(vaultId: String): Flow<List<VaultItem>>
    fun searchItems(vaultId: String, query: String): Flow<List<VaultItem>>
    suspend fun insert(item: VaultItem): AppResult<Unit>
    suspend fun insertBatch(items: List<VaultItem>): AppResult<Unit>
    suspend fun update(item: VaultItem): AppResult<Unit>
    suspend fun softDelete(id: String): AppResult<Unit>
    suspend fun restore(id: String): AppResult<Unit>
    suspend fun permanentDelete(id: String): AppResult<Unit>
}
