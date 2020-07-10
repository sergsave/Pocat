package com.sergsave.purryourcat.sharing

import com.sergsave.purryourcat.models.*
import kotlinx.serialization.*

@Serializable
data class Pack(val cat: CatData)

@Serializable
data class Bundle(val version: Int, val pack: Pack) {
    companion object { val ACTUAL_VERSION = 1 }
}
