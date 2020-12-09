package com.sergsave.pocat.helpers

import kotlinx.serialization.*
import android.net.Uri

@Serializer(forClass = Uri::class)
object UriSerializer : KSerializer<Uri> {
    override fun deserialize(decoder: Decoder): Uri {
        return Uri.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}