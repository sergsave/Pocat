package com.sergsave.purryourcat.sharing

import java.io.File

interface DataPacker {
    fun pack(pack: Pack, dir: File): File?
    fun unpack(file: File): Pack?
}