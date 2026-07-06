package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.ItemType
import com.passgo.app.core.model.VaultItem
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultItemRepositoryImpl @Inject constructor(
    private val vaultItemDao: VaultItemDao
) : VaultItemRepository {

    override fun getActiveItems(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getActiveItems(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getByType(vaultId: String, type: ItemType): Flow<List<VaultItem>> =
        vaultItemDao.getByType(vaultId, type.name).map { list -> list.map { it.toDomain() } }

    override fun getByFolder(folderId: String): Flow<List<VaultItem>> =
        vaultItemDao.getByFolder(folderId).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getFavorites(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getDeleted(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getDeleted(vaultId).map { list -> list.map { it.toDomain() } }

    override fun searchItems(vaultId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchItems(vaultId, query).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(item: VaultItem): AppResult<Unit> = runCatching {
        vaultItemDao.insert(item.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun insertBatch(items: List<VaultItem>): AppResult<Unit> = runCatching {
        vaultItemDao.insertBatch(items.map { it.toEntity() })
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(item: VaultItem): AppResult<Unit> = runCatching {
        vaultItemDao.update(item.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun restore(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.restore(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun permanentDelete(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.permanentDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
