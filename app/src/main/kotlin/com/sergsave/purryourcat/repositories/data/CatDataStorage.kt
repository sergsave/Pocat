package com.sergsave.purryourcat.repositories.data

import com.sergsave.purryourcat.models.CatData

interface CatDataStorage {
    fun save(cats: Map<String, CatData>?)
    fun load() : Map<String, CatData>?
}