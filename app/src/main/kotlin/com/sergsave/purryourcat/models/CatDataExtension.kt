package com.sergsave.purryourcat.models

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

fun CatData.combineContent(other: CatData, transform: (Uri?, Uri?)->Uri?): CatData {
    return copy(
        photoUri = transform(this.photoUri, other.photoUri),
        purrAudioUri = transform(this.purrAudioUri, other.purrAudioUri)
    )
}