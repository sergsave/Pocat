package com.sergsave.purryourcat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CatCard(
    val data: CatData,
    val dataRepoId: String?,
    val isShareable: Boolean,
    val isSaveable: Boolean
) : Parcelable

// TODO? Separate on CatData, Cat(id, data), CatCard(cat, isShareable)
