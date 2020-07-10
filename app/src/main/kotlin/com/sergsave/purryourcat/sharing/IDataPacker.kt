package com.sergsave.purryourcat.sharing

import android.net.Uri
import com.sergsave.purryourcat.models.CatData
import java.io.File

// Note. Packer is synchronous
interface IDataPacker {
    fun pack(pack: Pack): File?
    fun unpack(file: File): Pack?
}