package com.sergsave.purryourcat.sharing

import java.io.File
import io.reactivex.rxjava3.core.Single

abstract class DataPacker(protected val tempDir: File) {
    abstract fun pack(pack: Pack): Single<File>
    abstract fun unpack(file: File): Single<Pack>
}

interface DataPackerFactory {
    fun make(tempDir: File): DataPacker
}
