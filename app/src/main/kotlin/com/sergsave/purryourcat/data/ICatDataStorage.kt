package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData

interface ICatDataStorage {
    fun save(cats: Map<String, CatData>?)
    fun load() : Map<String, CatData>?
}