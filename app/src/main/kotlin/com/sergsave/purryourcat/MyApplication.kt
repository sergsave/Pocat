package com.sergsave.purryourcat

import android.app.Application
import android.content.Context
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.content.InternalFilesDirContentStorage
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.data.RoomCatDataStorage
import com.sergsave.purryourcat.helpers.FirstLaunchChecker
import com.sergsave.purryourcat.sharing.SendAnywhereNetworkService
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.sharing.WebSharingManager
import com.sergsave.purryourcat.sharing.ZipDataPacker
import com.sergsave.purryourcat.viewmodels.CatCardViewModelFactory
import com.sergsave.purryourcat.viewmodels.CatsListViewModelFactory
import com.sergsave.purryourcat.sampleprovider.SampleProvider

// Manual dependency injection
class AppContainer(context: Context) {
    private val catDataRepo = CatDataRepository(RoomCatDataStorage(context))
    private val contentRepo = ContentRepository(InternalFilesDirContentStorage(context))

    init { addSamples(context) }

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

    private fun addSamples(context: Context) {
        val preferences = context.getSharedPreferences(Constants.FIRST_LAUNCH_SHARED_PREFS_NAME, 0)
        if(FirstLaunchChecker(preferences).check())
            SampleProvider(context).provide().forEach { catDataRepo.add(it) }
    }
}

class MyApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        appContainer // init
    }
}