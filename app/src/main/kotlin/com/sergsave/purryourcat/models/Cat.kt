package com.sergsave.purryourcat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Cat(
    val id: UUID = UUID.randomUUID(),
    val data: CatData
) : Parcelable