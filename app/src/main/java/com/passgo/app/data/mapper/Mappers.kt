package com.passgo.app.data.mapper

import com.passgo.app.core.database.entity.AttachmentEntity
import com.passgo.app.core.database.entity.FolderEntity
import com.passgo.app.core.database.entity.TagEntity
import com.passgo.app.core.database.entity.VaultEntity
import com.passgo.app.core.database.entity.VaultItemEntity
import com.passgo.app.core.model.Attachment
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.ItemType
import com.passgo.app.core.model.SyncStatus
import com.passgo.app.core.model.Tag
import com.passgo.app.core.model.Vault
import com.passgo.app.core.model.VaultItem

fun VaultEntity.toDomain(): Vault = Vault(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Vault.toEntity(): VaultEntity = VaultEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = syncStatus.name
)

fun VaultItemEntity.toDomain(): VaultItem = VaultItem(
    id = id,
    vaultId = vaultId,
    folderId = folderId,
    type = ItemType.valueOf(type),
    name = name,
    username = username,
    password = password,
    url = url,
    notes = notes,
    favorite = favorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun VaultItem.toEntity(): VaultItemEntity = VaultItemEntity(
    id = id,
    vaultId = vaultId,
    folderId = folderId,
    type = type.name,
    name = name,
    username = username,
    password = password,
    url = url,
    notes = notes,
    favorite = favorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = syncStatus.name
)

fun FolderEntity.toDomain(): Folder = Folder(
    id = id,
    vaultId = vaultId,
    name = name,
    icon = icon,
    parentId = parentId,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Folder.toEntity(): FolderEntity = FolderEntity(
    id = id,
    vaultId = vaultId,
    name = name,
    icon = icon,
    parentId = parentId,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = syncStatus.name
)

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    vaultId = vaultId,
    name = name,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    vaultId = vaultId,
    name = name,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = syncStatus.name
)

fun AttachmentEntity.toDomain(): Attachment = Attachment(
    id = id,
    itemId = itemId,
    name = name,
    mimeType = mimeType,
    encryptedFileUri = encryptedFileUri,
    sizeBytes = sizeBytes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Attachment.toEntity(): AttachmentEntity = AttachmentEntity(
    id = id,
    itemId = itemId,
    name = name,
    mimeType = mimeType,
    encryptedFileUri = encryptedFileUri,
    sizeBytes = sizeBytes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncVersion = syncVersion,
    syncStatus = syncStatus.name
)
