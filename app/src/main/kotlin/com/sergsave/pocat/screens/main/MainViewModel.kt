package com.sergsave.pocat.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.pocat.billing.BillingRepository
import com.sergsave.pocat.content.ContentRepository
import com.sergsave.pocat.helpers.DisposableViewModel
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.helpers.Result
import com.sergsave.pocat.models.extractContent
import com.sergsave.pocat.persistent.CatDataRepository
import com.sergsave.pocat.preference.PreferenceManager
import com.sergsave.pocat.screens.main.analytics.MainAnalyticsHelper
import com.sergsave.pocat.sharing.WebSharingManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.Flowables
import timber.log.Timber

class MainViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    private val sharingManager: WebSharingManager,
    private val preferences: PreferenceManager,
    private val billingRepo: BillingRepository,
    private val analytics: MainAnalyticsHelper
): DisposableViewModel() {

    private val tabInfo2tag = mapOf<TabInfo, String>(
        TabInfo.SAMPLES to "samples",
        TabInfo.USER_CATS to "user_cats"
    )

    private val tag2tabInfo = tabInfo2tag.map { Pair(it.value, it.key) }.toMap()

    private val _shouldShowAppRate = MutableLiveData<Boolean>(false)
    val shouldShowAppRate: LiveData<Boolean>
        get() = _shouldShowAppRate

    private val _requestPageChangeEvent = MutableLiveData<Event<Int>>()
    val requestPageChangeEvent: LiveData<Event<Int>>
        get() = _requestPageChangeEvent

    init {
        val tabInfo = preferences.lastTabTag?.let { tag2tabInfo.get(it) }
        tabInfo?.let { _requestPageChangeEvent.value = Event(it.pageNumber) }

        analytics.onAppStarted()
    }

    fun tabInfoForPosition(position: Int): TabInfo? {
        return tabInfo2tag.keys.find { it.pageNumber == position }
    }

    fun onFirstActivityStarted() {
        // Do this not in Application, because Application is created only after device reload
        cleanUpSharingCache()
        cleanUpUnusedContent()

        processPendingPurchases()
    }

    fun onPageChanged(position: Int) {
        val tabInfo = tabInfoForPosition(position)
        val tag = tabInfo?.let { tabInfo2tag.get(it) }
        tag?.let { preferences.lastTabTag = it }

        tabInfo?.let { analytics.onTabOpened(it) }
    }

    fun onOptionsItemSelected(menuId: Int) = analytics.onOptionsItemSelected(menuId)

    fun onForwardIntent() {
        _requestPageChangeEvent.value = Event(TabInfo.USER_CATS.pageNumber)
    }

    fun onAppRateShowed() {
        analytics.onAppRateShowed()
        _shouldShowAppRate.value = false
    }

    fun onAppRateAccepted() {
        analytics.onAppRateAccepted()
    }

    fun onAppRateShowLater() {
        analytics.onAppRatePostponed()
    }

    fun onAppRateDeclined() {
        analytics.onAppRateDeclined()
    }

    fun onCatWasPetted() {
        _shouldShowAppRate.value = true
    }

    private fun cleanUpSharingCache() {
        addDisposable(sharingManager.cleanup().subscribe({}, {
            Timber.e(it, "Cleanup failed")
        }))
    }

    private fun cleanUpUnusedContent() {
        val disposable = Flowables.zip(catDataRepository.read(), contentRepository.read())
            .take(1)
            .flatMap { (data, content) ->
                val usedContent = data.flatMap { (_, cat) -> cat.data.extractContent() }
                val unusedContent = content - usedContent

                Observable.fromIterable(unusedContent)
                    .concatMapCompletable { contentRepository.remove(it) }
                    .toFlowable<Unit>()
            }
            .subscribe({}, {
                Timber.e(it, "Cleanup failed")
            })
        addDisposable(disposable)
    }

    private fun processPendingPurchases() {
        val disposable = billingRepo.connectToBillingService()
            .filter { it is Result.Success }
            .map { billingRepo.processPendingPurchases() }
            .subscribe()
        addDisposable(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        billingRepo.disconnectFromBillingService()
    }
}

