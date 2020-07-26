package com.sergsave.purryourcat.sharing

import java.io.File

interface IDataPacker {
    fun pack(pack: Pack): File?
    fun unpack(file: File): Pack?
}