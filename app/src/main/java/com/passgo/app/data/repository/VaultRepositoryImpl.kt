package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.VaultDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Vault
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(
    private val vaultDao: VaultDao
) : VaultRepository {

    override fun getActiveVault(): Flow<Vault?> = vaultDao.getActiveVault().map { it?.toDomain() }

    override fun getVaultById(id: String): Flow<Vault?> = vaultDao.getVaultById(id).map { it?.toDomain() }

    override suspend fun insert(vault: Vault): AppResult<Unit> = runCatching {
        vaultDao.insert(vault.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(vault: Vault): AppResult<Unit> = runCatching {
        vaultDao.update(vault.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        vaultDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
