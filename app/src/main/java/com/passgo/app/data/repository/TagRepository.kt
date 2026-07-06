package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getActiveTags(vaultId: String): Flow<List<Tag>>
    fun getTagById(id: String): Flow<Tag?>
    fun getTagsForItem(itemId: String): Flow<List<Tag>>
    suspend fun insert(tag: Tag): AppResult<Unit>
    suspend fun update(tag: Tag): AppResult<Unit>
    suspend fun softDelete(id: String): AppResult<Unit>
    suspend fun addTagToItem(tagId: String, itemId: String): AppResult<Unit>
    suspend fun removeTagFromItem(tagId: String, itemId: String): AppResult<Unit>
}
