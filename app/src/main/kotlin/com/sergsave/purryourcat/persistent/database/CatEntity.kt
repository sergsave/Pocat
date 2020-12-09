package com.sergsave.pocat.persistent.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cats")
data class Cat(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "created_time") val createdTime: Long,
    @Embedded val entity: BaseCatEntity
)

data class CatWithoutTime(
    val id: String,
    @Embedded val entity: BaseCatEntity
)

data class BaseCatEntity(
    val name: String?,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    @ColumnInfo(name = "audio_uri") val audioUri: String?
)