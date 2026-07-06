package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.TagDao
import com.passgo.app.core.database.entity.TagItemCrossRef
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Tag
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun getActiveTags(vaultId: String): Flow<List<Tag>> =
        tagDao.getActiveTags(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getTagById(id: String): Flow<Tag?> =
        tagDao.getTagById(id).map { it?.toDomain() }

    override fun getTagsForItem(itemId: String): Flow<List<Tag>> =
        tagDao.getTagsForItem(itemId).map { list -> list.map { it.toDomain() } }

    override fun searchTags(vaultId: String, query: String): Flow<List<Tag>> =
        tagDao.searchTags(vaultId, query).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(tag: Tag): AppResult<Unit> = runCatching {
        tagDao.insert(tag.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(tag: Tag): AppResult<Unit> = runCatching {
        tagDao.update(tag.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        tagDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun addTagToItem(tagId: String, itemId: String): AppResult<Unit> = runCatching {
        tagDao.addTagToItem(TagItemCrossRef(tagId, itemId))
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun removeTagFromItem(tagId: String, itemId: String): AppResult<Unit> = runCatching {
        tagDao.removeTagFromItem(tagId, itemId)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun setItemTags(itemId: String, tagIds: List<String>): AppResult<Unit> = runCatching {
        tagDao.removeAllTagsFromItem(itemId)
        tagIds.forEach { tagId ->
            tagDao.addTagToItem(TagItemCrossRef(tagId, itemId))
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
