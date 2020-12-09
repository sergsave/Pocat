package com.sergsave.pocat.models

import android.net.Uri

fun CatData.extractContent(): List<Uri> {
    return listOfNotNull(photoUri, purrAudioUri)
}

fun CatData.withUpdatedContent(transform: (Uri?)->Uri?): CatData {
    return copy(
        photoUri = transform(this.photoUri),
        purrAudioUri = transform(this.purrAudioUri)
    )
}