package com.sergsave.purryourcat.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.sergsave.purryourcat.data.database.Cat
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.data.database.CatDatabase

class RoomCatDataStorage(context: Context): CatDataStorage {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CatDatabase::class.java, "cat_database"
    )
        .allowMainThreadQueries() // TODO: Async interface for storage
        .build()


    override fun load(): Map<String, CatData> {
        return database.catDao().getAll().associate {
            val catData = CatData(
                name = it.name,
                photoUri = it.photoUri?.let { Uri.parse(it) },
                purrAudioUri = it.audioUri?.let { Uri.parse(it) }
            )
            Pair(it.id, catData)
        }
    }

    override fun save(cats: Map<String, CatData>) {
        val entities = cats.map { (k, v) ->
            Cat(
                id = k,
                name = v.name,
                photoUri = v.photoUri?.toString(),
                audioUri = v.purrAudioUri?.toString()
            )
        }
        database.catDao().deleteAndInsertAll(entities)
    }
}