package com.sergsave.pocat.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.sergsave.pocat.content.ContentRepository
import com.sergsave.pocat.persistent.CatDataRepository
import com.sergsave.pocat.helpers.DisposableViewModel
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.models.extractContent
import com.sergsave.pocat.preference.PreferenceManager
import com.sergsave.pocat.sharing.WebSharingManager
import com.sergsave.pocat.R
import com.sergsave.pocat.screens.main.analytics.MainAnalyticsHelper
import io.reactivex.Observable
import io.reactivex.rxkotlin.Flowables

class MainViewModel(
    private val catDataRepository: CatDataRepository,
    private val contentRepository: ContentRepository,
    private val sharingManager: WebSharingManager,
    private val preferences: PreferenceManager,
    private val analytics: MainAnalyticsHelper
): DisposableViewModel() {

    private var pagePosition: Int? = null

    private val tabInfo2tag = mapOf<TabInfo, String>(
        TabInfo.SAMPLES to "samples",
        TabInfo.USER_CATS to "user_cats"
    )

    private val tag2tabInfo = tabInfo2tag.map { Pair(it.value, it.key) }.toMap()

    private val _clearSelectionEvent = MutableLiveData<Event<Unit>>()
    val clearSelectionEvent: LiveData<Event<Unit>>
        get() = _clearSelectionEvent

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

    fun cleanUnusedFiles() {
        addDisposable(sharingManager.cleanup().subscribe({}, { Log.e(TAG, "Cleanup failed", it) }))
        cleanUpUnusedContent()
    }

    fun onPageChanged(position: Int) {
        if(position != pagePosition)
            _clearSelectionEvent.value = Event(Unit)

        pagePosition = position
        val tabInfo = tabInfoForPosition(position)
        val tag = tabInfo?.let { tabInfo2tag.get(it) }
        tag?.let { preferences.lastTabTag = it }

        tabInfo?.let { analytics.onTabOpened(it) }
    }

    fun onOptionsItemSelected(menuId: Int) = analytics.onOptionsItemSelected(menuId)

    fun onForwardIntent() {
        _requestPageChangeEvent.value = Event(TabInfo.USER_CATS.pageNumber)
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
            .subscribe({}, { Log.e(TAG, "Cleanup failed", it) })
        addDisposable(disposable)
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

