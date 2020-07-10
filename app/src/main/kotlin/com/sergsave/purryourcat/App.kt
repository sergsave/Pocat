package com.sergsave.purryourcat

import android.app.Application
import android.net.Uri
import com.sergsave.purryourcat.content.ContentRepo
import com.sergsave.purryourcat.content.InternalFilesDirContentStorage
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.data.SharedPreferencesCatDataStorage
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.sharing.WebSharingManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        CatDataRepo.init(SharedPreferencesCatDataStorage(this))
        ContentRepo.init(InternalFilesDirContentStorage(this))
        SharingManager.init(WebSharingManager(context = this, cleanCacheOnCreate = true))

        cleanUpUnusedContent()
    }

    private fun cleanUpUnusedContent() {
        val usedContent = ArrayList<Uri>()
        CatDataRepo.instance?.read()?.value?.forEach{(_, cat) ->
            usedContent.addAll(cat.extractContent())
        }
        val allContent = ContentRepo.instance?.read()?.value
        allContent?.let { all -> (all - usedContent).forEach{
            ContentRepo.instance?.remove(it)
        }}
    }
}