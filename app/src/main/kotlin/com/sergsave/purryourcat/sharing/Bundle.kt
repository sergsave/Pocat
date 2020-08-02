package com.sergsave.purryourcat.sharing

import com.sergsave.purryourcat.models.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.*

@Serializable
@Parcelize
data class Pack(val cat: CatData): Parcelable

@Serializable
data class Bundle(val version: Int, val pack: Pack) {
    companion object { val ACTUAL_VERSION = 1 }
}
