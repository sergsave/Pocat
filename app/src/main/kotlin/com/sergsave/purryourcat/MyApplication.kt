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
import com.sergsave.purryourcat.helpers.FirstLaunchChecker
import com.sergsave.purryourcat.helpers.ViewModelFactory
import com.sergsave.purryourcat.preference.PreferenceManager
import com.sergsave.purryourcat.samples.CatSampleProvider
import com.sergsave.purryourcat.samples.SoundSampleProvider
import com.sergsave.purryourcat.sharing.FirebaseCloudSharingManager
import com.sergsave.purryourcat.sharing.SharingManager
import com.sergsave.purryourcat.sharing.ZipDataPackerFactory
import com.sergsave.purryourcat.ui.catcard.FormViewModel
import com.sergsave.purryourcat.ui.catcard.PurringViewModel
import com.sergsave.purryourcat.ui.catcard.SharingDataExtractViewModel
import com.sergsave.purryourcat.ui.main.MainViewModel
import com.sergsave.purryourcat.ui.main.UserCatsViewModel
import com.sergsave.purryourcat.ui.main.SamplesViewModel
import com.sergsave.purryourcat.ui.soundselection.SoundSelectionViewModel
import io.reactivex.Observable

// Manual dependency injection
class AppContainer(private val context: Context) {
    private val catDataRepo = CatDataRepository(RoomCatDataStorage(context))
    private val imageStorage = LocalFilesContentStorage(context, ImageResizeSavingStrategy(context))
    private val audioStorage = LocalFilesContentStorage(context, CopySavingStrategy(context))
    private val contentRepo = ContentRepository(imageStorage, audioStorage)
    private val preferences = PreferenceManager(context)
    private val maxAudioFileSizeMB = 2L

    private val sharingManager: SharingManager =
         FirebaseCloudSharingManager(context, ZipDataPackerFactory(context))
    private val sharingErrorStringId = R.string.connection_error

    val soundSampleProvider = SoundSampleProvider(context)

    fun provideMainViewModelFactory() =
        ViewModelFactory(MainViewModel::class.java, {
            MainViewModel(catDataRepo, contentRepo, sharingManager)
        })

    fun provideSamplesViewModelFactory() =
        ViewModelFactory(SamplesViewModel::class.java, {
            SamplesViewModel(CatSampleProvider(context))
        })

    fun provideUserCatsViewModelFactory() =
        ViewModelFactory(UserCatsViewModel::class.java, {
            UserCatsViewModel(catDataRepo)
        })

    fun provideFormViewModelFactory(catId: String?) =
        ViewModelFactory(FormViewModel::class.java, {
            FormViewModel(catDataRepo, contentRepo, catId)
        })

    fun providePurringViewModelFactory(cat: PurringViewModel.Cat) =
        ViewModelFactory(PurringViewModel::class.java, {
            PurringViewModel(catDataRepo, sharingManager, preferences, sharingErrorStringId, cat)
        })

    fun provideSharingDataExtractViewModelFactory() =
        ViewModelFactory(SharingDataExtractViewModel::class.java, {
            SharingDataExtractViewModel(sharingManager, contentRepo, sharingErrorStringId)
        })

    fun provideSoundSelectionViewModelFactory() =
        ViewModelFactory(SoundSelectionViewModel::class.java, {
            SoundSelectionViewModel(context, maxAudioFileSizeMB)
        })
}

class MyApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate() {
        super.onCreate()

        appContainer // init
    }
}

//    init { addSamples(context) }

//    private fun addSamples(context: Context) {
//        val preferences = context.getSharedPreferences(Constants.FIRST_LAUNCH_SHARED_PREFS_NAME, 0)
//        if(FirstLaunchChecker(preferences).check()) {
//            // TODO: Synchronous add to avoid recycler view shuffle on first start
//            val samples = CatSampleProvider(context).provide().toMutableList()
//            Observable.fromIterable(samples).concatMapSingle{ catDataRepo.add(it) }.subscribe{ }
//        }
//    }