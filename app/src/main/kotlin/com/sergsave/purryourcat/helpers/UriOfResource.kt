package com.sergsave.purryourcat.helpers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri

fun uriOfResource(resId: Int, context: Context): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.resources.getResourcePackageName(resId))
        .appendPath(context.resources.getResourceTypeName(resId))
        .appendPath(context.resources.getResourceEntryName(resId))
        .build()
}