package com.sergsave.purryourcat.data

import com.sergsave.purryourcat.models.CatData
import io.reactivex.rxjava3.core.Single

interface CatDataStorage {
    fun save(cats: Map<String, CatData>): Single<Unit>
    fun load(): Single<Map<String, CatData>>
}