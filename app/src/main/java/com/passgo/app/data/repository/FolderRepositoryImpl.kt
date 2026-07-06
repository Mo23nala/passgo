package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.FolderDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Folder
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getActiveFolders(vaultId: String): Flow<List<Folder>> =
        folderDao.getActiveFolders(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getFolderById(id: String): Flow<Folder?> =
        folderDao.getFolderById(id).map { it?.toDomain() }

    override suspend fun insert(folder: Folder): AppResult<Unit> = runCatching {
        folderDao.insert(folder.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(folder: Folder): AppResult<Unit> = runCatching {
        folderDao.update(folder.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        folderDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun permanentDelete(id: String): AppResult<Unit> = runCatching {
        folderDao.permanentDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun rename(id: String, newName: String): AppResult<Unit> = runCatching {
        folderDao.rename(id, newName)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
