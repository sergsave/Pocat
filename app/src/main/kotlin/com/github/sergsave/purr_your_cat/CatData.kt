package com.github.sergsave.purr_your_cat

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CatData(
    var name: String? = null,
    var photoUri: Uri? = null,
    var purrAudioUri: Uri? = null
) : Parcelable