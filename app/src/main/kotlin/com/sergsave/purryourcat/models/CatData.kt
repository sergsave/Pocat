package com.sergsave.purryourcat.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CatData(
    val name: String? = null,
    val photoUri: Uri? = null,
    val purrAudioUri: Uri? = null
) : Parcelable