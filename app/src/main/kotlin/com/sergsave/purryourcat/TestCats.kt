package com.sergsave.purryourcat

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.content.ContentRepo
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.models.CatData
import com.sergsave.purryourcat.models.withUpdatedContent

fun addTestCats(context: Context) {
    val testUri = Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(R.drawable.cat)
                + '/' + context.getResources().getResourceTypeName(R.drawable.cat)
                + '/' + context.getResources().getResourceEntryName(R.drawable.cat))

    val testCats = arrayListOf(
        CatData("Simka", testUri),
        CatData("Masik", testUri),
        CatData("Uta", testUri),
        CatData("Sherya", testUri),
        CatData("Sema", testUri),
        CatData("Philya", testUri),
        CatData("Ganya", testUri)
    )

    if(CatDataRepo.instance?.read()?.value?.isEmpty() ?: false)
        testCats.forEach { cat ->
            val updatedContent = cat.withUpdatedContent{ uri -> ContentRepo.instance?.add(uri) }
            CatDataRepo.instance?.add(updatedContent)
        }
}