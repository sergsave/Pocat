package com.sergsave.purryourcat

import android.app.Application
import android.net.Uri
import com.sergsave.purryourcat.content.ContentRepo
import com.sergsave.purryourcat.content.InternalFilesDirContentStorage
import com.sergsave.purryourcat.data.CatDataRepo
import com.sergsave.purryourcat.data.SharedPreferencesCatDataStorage
import com.sergsave.purryourcat.models.extractContent
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.sharing.WebSharingStrategy
import com.sergsave.purryourcat.sharing.SendAnywhereNetworkService

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        CatDataRepo.init(SharedPreferencesCatDataStorage(this))
        ContentRepo.init(InternalFilesDirContentStorage(this))
        val sharingImpl = WebSharingStrategy(
            context = this,
            service = SendAnywhereNetworkService(this), // TODO: Firebase impl?
            cleanCacheOnCreate = true)
        SharingManager.init(sharingImpl)

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