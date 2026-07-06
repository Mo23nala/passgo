package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Vault
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun getActiveVault(): Flow<Vault?>
    fun getVaultById(id: String): Flow<Vault?>
    suspend fun insert(vault: Vault): AppResult<Unit>
    suspend fun update(vault: Vault): AppResult<Unit>
    suspend fun softDelete(id: String): AppResult<Unit>
}
