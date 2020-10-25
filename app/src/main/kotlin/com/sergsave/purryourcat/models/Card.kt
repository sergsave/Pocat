package com.sergsave.purryourcat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Card(
    val cat: Cat,
    val isSaveable: Boolean,
    val isShareable: Boolean
) : Parcelable