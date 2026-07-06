package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Attachment
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAttachmentsForItem(itemId: String): Flow<List<Attachment>>
    fun getAttachmentById(id: String): Flow<Attachment?>
    suspend fun insert(attachment: Attachment): AppResult<Unit>
    suspend fun update(attachment: Attachment): AppResult<Unit>
    suspend fun softDelete(id: String): AppResult<Unit>
}
