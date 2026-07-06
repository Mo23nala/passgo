package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passgo.app.core.database.entity.VaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vaults WHERE deleted_at IS NULL LIMIT 1")
    fun getActiveVault(): Flow<VaultEntity?>

    @Query("SELECT * FROM vaults WHERE id = :id")
    fun getVaultById(id: String): Flow<VaultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vault: VaultEntity)

    @Update
    suspend fun update(vault: VaultEntity)

    @Query("UPDATE vaults SET deleted_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
