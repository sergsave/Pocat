package com.sergsave.pocat.sharing

import com.sergsave.pocat.models.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.*

@Serializable
@Parcelize
data class Pack(val cat: CatData): Parcelable
