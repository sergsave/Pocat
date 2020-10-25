package com.sergsave.purryourcat.persistent.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.PrimaryKey

@Entity(tableName = "cats", primaryKeys = arrayOf("id"))
data class TimedCatEntity(
    @ColumnInfo(name = "created_time") val createdTime: Long,
    @Embedded val base: CatEntity
)

data class CatEntity(
    val id: String,
    val name: String?,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    @ColumnInfo(name = "audio_uri") val audioUri: String?
)

//data class BaseCatEntity(
//    val name: String?,
//    @ColumnInfo(name = "photo_uri") val photoUri: String?,
//    @ColumnInfo(name = "audio_uri") val audioUri: String?
//)

//@Entity(tableName = "cats")
//data class TimedCatEntity(
//    @PrimaryKey val id: String,
//    @ColumnInfo(name = "created_time") val createdTime: Long,
//    @Embedded val entity: BaseCatEntity
//)
//
//data class CatEntity(
//    val id: String,
//    @Embedded val entity: BaseCatEntity
//)
//
//data class BaseCatEntity(
//    val name: String?,
//    @ColumnInfo(name = "photo_uri") val photoUri: String?,
//    @ColumnInfo(name = "audio_uri") val audioUri: String?
//)