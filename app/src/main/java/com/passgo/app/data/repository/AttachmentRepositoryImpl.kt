package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.AttachmentDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Attachment
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao
) : AttachmentRepository {

    override fun getAttachmentsForItem(itemId: String): Flow<List<Attachment>> =
        attachmentDao.getAttachmentsForItem(itemId).map { list -> list.map { it.toDomain() } }

    override fun getAttachmentById(id: String): Flow<Attachment?> =
        attachmentDao.getAttachmentById(id).map { it?.toDomain() }

    override suspend fun insert(attachment: Attachment): AppResult<Unit> = runCatching {
        attachmentDao.insert(attachment.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(attachment: Attachment): AppResult<Unit> = runCatching {
        attachmentDao.update(attachment.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        attachmentDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
