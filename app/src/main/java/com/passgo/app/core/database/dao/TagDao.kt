package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passgo.app.core.database.entity.TagEntity
import com.passgo.app.core.database.entity.TagItemCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE vault_id = :vaultId AND deleted_at IS NULL ORDER BY name ASC")
    fun getActiveTags(vaultId: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getTagById(id: String): Flow<TagEntity?>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN tag_item ti ON t.id = ti.tag_id
        WHERE ti.item_id = :itemId AND t.deleted_at IS NULL
        ORDER BY t.name ASC
    """)
    fun getTagsForItem(itemId: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE vault_id = :vaultId AND deleted_at IS NULL AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(vaultId: String, query: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Update
    suspend fun update(tag: TagEntity)

    @Query("UPDATE tags SET deleted_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToItem(crossRef: TagItemCrossRef)

    @Query("DELETE FROM tag_item WHERE tag_id = :tagId AND item_id = :itemId")
    suspend fun removeTagFromItem(tagId: String, itemId: String)

    @Query("DELETE FROM tag_item WHERE item_id = :itemId")
    suspend fun removeAllTagsFromItem(itemId: String)
}
