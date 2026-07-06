package com.passgo.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tag_item",
    primaryKeys = ["tag_id", "item_id"],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VaultItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tag_id"]),
        Index(value = ["item_id"])
    ]
)
data class TagItemCrossRef(
    @ColumnInfo(name = "tag_id")
    val tagId: String,
    @ColumnInfo(name = "item_id")
    val itemId: String
)
