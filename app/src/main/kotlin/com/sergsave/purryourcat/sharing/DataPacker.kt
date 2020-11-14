package com.sergsave.purryourcat.sharing

import java.io.File
import io.reactivex.Single

interface DataPacker {
    fun pack(pack: Pack, buildDir: File): Single<File>
    fun unpack(file: File, buildDir: File): Single<Pack>
}