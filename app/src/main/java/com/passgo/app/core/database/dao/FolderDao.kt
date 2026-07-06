package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passgo.app.core.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE vault_id = :vaultId AND deleted_at IS NULL ORDER BY sort_order ASC")
    fun getActiveFolders(vaultId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun getFolderById(id: String): Flow<FolderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(folders: List<FolderEntity>)

    @Update
    suspend fun update(folder: FolderEntity)

    @Query("UPDATE folders SET deleted_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun permanentDelete(id: String)
}
