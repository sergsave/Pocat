package com.sergsave.pocat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(
    val persistentId: String?,
    val data: CatData,
    val isSaveable: Boolean,
    val isShareable: Boolean
) : Parcelable