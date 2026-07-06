package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passgo.app.core.database.entity.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {

    @Query("SELECT COUNT(*) FROM vault_items WHERE deleted_at IS NULL")
    fun getActiveItemsCount(): Flow<Int>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL ORDER BY updated_at DESC")
    fun getActiveItems(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND type = :type ORDER BY updated_at DESC")
    fun getByType(vaultId: String, type: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE deleted_at IS NULL AND folder_id = :folderId ORDER BY updated_at DESC")
    fun getByFolder(folderId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND favorite = 1 ORDER BY updated_at DESC")
    fun getFavorites(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getDeleted(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT * FROM vault_items 
        WHERE vault_id = :vaultId AND deleted_at IS NULL 
        AND (name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        ORDER BY updated_at DESC
    """)
    fun searchItems(vaultId: String, query: String): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(items: List<VaultItemEntity>)

    @Update
    suspend fun update(item: VaultItemEntity)

    @Query("UPDATE vault_items SET deleted_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_items SET deleted_at = NULL, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun permanentDelete(id: String)
}
