package com.sergsave.purryourcat.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CatData(
    var name: String? = null,
    var photoUri: Uri? = null,
    var purrAudioUri: Uri? = null
) : Parcelable