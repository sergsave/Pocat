package com.sergsave.purryourcat

import android.app.Application
import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.content.CopySavingStrategy
import com.sergsave.purryourcat.content.ImageResizeSavingStrategy
import com.sergsave.purryourcat.content.LocalFilesContentStorage
import com.sergsave.purryourcat.data.CatDataRepository
import com.sergsave.purryourcat.data.RoomCatDataStorage
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.helpers.FirstLaunchChecker
import com.sergsave.purryourcat.helpers.ViewModelFactory
import com.sergsave.purryourcat.preference.PreferenceReader
import com.sergsave.purryourcat.sampleprovider.SampleProvider
import com.sergsave.purryourcat.sharing.FirebaseNetworkService
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.sharing.WebSharingManager
import com.sergsave.purryourcat.sharing.ZipDataPacker
import com.sergsave.purryourcat.ui.catcard.FormViewModel
import com.sergsave.purryourcat.ui.catcard.PurringViewModel
import com.sergsave.purryourcat.ui.catcard.SharingDataExtractViewModel
import com.sergsave.purryourcat.ui.catslist.CatsListViewModel
import io.reactivex.rxjava3.core.Observable

// Manual dependency injection
class AppContainer(context: Context) {
    private val catDataRepo = CatDataRepository(RoomCatDataStorage(context))
    private val imageStorage = LocalFilesContentStorage(context, ImageResizeSavingStrategy(context))
    private val audioStorage = LocalFilesContentStorage(context, CopySavingStrategy(context))
    private val preferences = PreferenceReader(context)

    private val contentRepo = ContentRepository(
        imageStorage,
        audioStorage,
        maxImageFileSize = Long.MAX_VALUE,
        maxAudioFileSize = 2 * 1024 * 1024
    )

    private val sharingManager: SharingManager =
        WebSharingManager(
            context,
            FirebaseNetworkService(),
            ZipDataPacker(context)
        )
    private val sharingErrorStringId = R.string.connection_error

    init { addSamples(context) }

    fun provideCatsListViewModelFactory() =
        ViewModelFactory(CatsListViewModel::class.java, {
            CatsListViewModel(catDataRepo, contentRepo, sharingManager)
        })

    fun provideFormViewModelFactory(catId: String?) =
        ViewModelFactory(FormViewModel::class.java, {
            FormViewModel(catDataRepo, contentRepo, fileHelper, catId)
        })

    fun providePurringViewModelFactory(cat: PurringViewModel.Cat) =
        ViewModelFactory(PurringViewModel::class.java, {
            PurringViewModel(catDataRepo, sharingManager, preferences, sharingErrorStringId, cat)
        })

    fun provideSharingDataExtractViewModelFactory() =
        ViewModelFactory(SharingDataExtractViewModel::class.java, {
            SharingDataExtractViewModel(sharingManager, contentRepo, sharingErrorStringId)
        })

    private val fileHelper = object: FormViewModel.FileHelper {
        override fun getFileName(uri: Uri) = FileUtils.getContentFileName(context, uri)
        override fun getFileSize(uri: Uri) = FileUtils.getContentFileSize(context, uri)
    }

    private fun addSamples(context: Context) {
        val preferences = context.getSharedPreferences(Constants.FIRST_LAUNCH_SHARED_PREFS_NAME, 0)
        if(FirstLaunchChecker(preferences).check()) {
            val samples = SampleProvider(context).provide().toMutableList()
            Observable.fromIterable(samples).concatMapSingle{ catDataRepo.add(it) }.subscribe{ }
        }
    }
}

class MyApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate() {
        super.onCreate()

        appContainer // init
    }
}