package com.sergsave.purryourcat.sharing

import android.net.Uri
import com.sergsave.purryourcat.models.CatData

interface ISharingManager {
    fun prepareSharingUri(catData: CatData?): Uri?
    fun extractFromSharingUri(uri: Uri?): CatData?
    fun mimeType(): String
}