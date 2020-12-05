package com.sergsave.purryourcat

import android.app.Application
import android.content.Context
import android.net.Uri
import com.sergsave.purryourcat.analytics.FirebaseAnalyticsTracker
import com.sergsave.purryourcat.content.ContentRepository
import com.sergsave.purryourcat.content.CopySavingStrategy
import com.sergsave.purryourcat.content.ImageResizeSavingStrategy
import com.sergsave.purryourcat.content.LocalFilesContentStorage
import com.sergsave.purryourcat.helpers.FileUtils
import com.sergsave.purryourcat.persistent.CatDataRepository
import com.sergsave.purryourcat.persistent.RoomCatDataStorage
import com.sergsave.purryourcat.helpers.ViewModelFactory
import com.sergsave.purryourcat.preference.PreferenceManager
import com.sergsave.purryourcat.samples.CatSampleProvider
import com.sergsave.purryourcat.samples.SoundSampleProvider
import com.sergsave.purryourcat.sharing.FirebaseCloudSharingManager
import com.sergsave.purryourcat.sharing.WebSharingManager
import com.sergsave.purryourcat.sharing.ZipDataPacker
import com.sergsave.purryourcat.models.Card
import com.sergsave.purryourcat.screens.catcard.FormViewModel
import com.sergsave.purryourcat.screens.catcard.PurringViewModel
import com.sergsave.purryourcat.screens.catcard.SharingDataExtractViewModel
import com.sergsave.purryourcat.screens.catcard.analytics.CatCardAnalyticsHelper
import com.sergsave.purryourcat.screens.main.MainViewModel
import com.sergsave.purryourcat.screens.main.UserCatsViewModel
import com.sergsave.purryourcat.screens.main.SamplesViewModel
import com.sergsave.purryourcat.screens.main.analytics.MainAnalyticsHelper
import com.sergsave.purryourcat.screens.settings.SettingsViewModel
import com.sergsave.purryourcat.screens.settings.analytics.SettingsAnalyticsHelper
import com.sergsave.purryourcat.screens.soundselection.SamplesListViewModel
import com.sergsave.purryourcat.screens.testing.TestingViewModel
import com.sergsave.purryourcat.screens.soundselection.SoundSelectionViewModel
import com.sergsave.purryourcat.screens.soundselection.analytics.SoundSelectionAnalyticsHelper

// Manual dependency injection
class AppContainer(context: Context) {
    private val catDataRepo = CatDataRepository(RoomCatDataStorage(context))
    private val imageStorage = LocalFilesContentStorage(context, ImageResizeSavingStrategy(context))
    private val audioStorage = LocalFilesContentStorage(context, CopySavingStrategy(context))
    private val contentRepo = ContentRepository(imageStorage, audioStorage)
    private val preferences = PreferenceManager(context)
    private val sharingManager = FirebaseCloudSharingManager(context, ZipDataPacker(context))
    private val analyticsTracker = FirebaseAnalyticsTracker()
    private val soundSampleProvider = SoundSampleProvider(context)
    private val catSampleProvider = CatSampleProvider(context)

    private val fileSizeCalculator = { uri: Uri -> FileUtils.resolveContentFileSize(context, uri) }

    private val mainAnalytics = MainAnalyticsHelper(analyticsTracker)
    private val soundSelectionAnalytics = SoundSelectionAnalyticsHelper(analyticsTracker)
    private val settingsAnalytics = SettingsAnalyticsHelper(analyticsTracker)
    private val catCardAnalytics = CatCardAnalyticsHelper(analyticsTracker, fileSizeCalculator)
    private val maxAudioFileSizeMB = 2L

    fun provideMainViewModelFactory() =
        ViewModelFactory(MainViewModel::class.java, {
            MainViewModel(catDataRepo, contentRepo, sharingManager, preferences, mainAnalytics)
        })

    fun provideSamplesViewModelFactory() =
        ViewModelFactory(SamplesViewModel::class.java, {
            SamplesViewModel(catSampleProvider, mainAnalytics)
        })

    fun provideUserCatsViewModelFactory() =
        ViewModelFactory(UserCatsViewModel::class.java, {
            UserCatsViewModel(catDataRepo, mainAnalytics)
        })

    fun provideFormViewModelFactory(card: Card?) =
        ViewModelFactory(FormViewModel::class.java, {
            FormViewModel(catDataRepo, contentRepo, card, catCardAnalytics)
        })

    fun providePurringViewModelFactory(card: Card) =
        ViewModelFactory(PurringViewModel::class.java, {
            PurringViewModel(catDataRepo, sharingManager, preferences, card, catCardAnalytics)
        })

    fun provideSharingDataExtractViewModelFactory() =
        ViewModelFactory(SharingDataExtractViewModel::class.java, {
            SharingDataExtractViewModel(sharingManager, contentRepo, catCardAnalytics)
        })

    fun provideSoundSelectionViewModelFactory() =
        ViewModelFactory(SoundSelectionViewModel::class.java, {
            SoundSelectionViewModel(fileSizeCalculator, maxAudioFileSizeMB, soundSelectionAnalytics)
        })

    fun provideSamplesListViewModelFactory() =
        ViewModelFactory(SamplesListViewModel::class.java, {
            SamplesListViewModel(soundSampleProvider)
        })

    fun provideSettingsViewModelFactory() =
        ViewModelFactory(SettingsViewModel::class.java, {
            SettingsViewModel(settingsAnalytics)
        })

    fun provideTestingViewModelFactory() =
        ViewModelFactory(TestingViewModel::class.java, {
            TestingViewModel(catDataRepo, contentRepo)
        })
}

class MyApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate() {
        super.onCreate()

        appContainer // init
    }
}
