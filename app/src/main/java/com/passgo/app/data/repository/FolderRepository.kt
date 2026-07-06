package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getActiveFolders(vaultId: String): Flow<List<Folder>>
    fun getFolderById(id: String): Flow<Folder?>
    suspend fun insert(folder: Folder): AppResult<Unit>
    suspend fun update(folder: Folder): AppResult<Unit>
    suspend fun softDelete(id: String): AppResult<Unit>
    suspend fun permanentDelete(id: String): AppResult<Unit>
}
