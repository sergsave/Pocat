package com.sergsave.purryourcat

import android.app.Application
import android.content.Context
import com.sergsave.purryourcat.repositories.content.ContentRepository
import com.sergsave.purryourcat.repositories.content.InternalFilesDirContentStorage
import com.sergsave.purryourcat.repositories.data.CatDataRepository
import com.sergsave.purryourcat.repositories.data.SharedPreferencesCatDataStorage
import com.sergsave.purryourcat.sharing.SendAnywhereNetworkService
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.sharing.WebSharingManager
import com.sergsave.purryourcat.sharing.ZipDataPacker
import com.sergsave.purryourcat.viewmodels.CatCardViewModelFactory
import com.sergsave.purryourcat.viewmodels.CatsListViewModelFactory

// Manual dependency injection
class AppContainer(private val context: Context) {
    private val catDataRepo = CatDataRepository(SharedPreferencesCatDataStorage(context))
    private val contentRepo = ContentRepository(InternalFilesDirContentStorage(context))

    fun provideCatCardViewModelFactory(catId: String?): CatCardViewModelFactory {
        return CatCardViewModelFactory(catDataRepo, contentRepo, catId)
    }

    fun provideCatsListViewModelFactory(): CatsListViewModelFactory {
        return CatsListViewModelFactory(catDataRepo, contentRepo)
    }

    // TODO: Firebase impl?
    val sharingManager: SharingManager =
        WebSharingManager(
            context,
            SendAnywhereNetworkService(context),
            ZipDataPacker(context)
        )
}

class MyApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(applicationContext) }
}