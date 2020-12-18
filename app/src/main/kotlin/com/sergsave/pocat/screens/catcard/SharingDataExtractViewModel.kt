package com.sergsave.pocat.screens.catcard

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sergsave.pocat.content.ContentRepository
import com.sergsave.pocat.helpers.DisposableViewModel
import com.sergsave.pocat.helpers.Event
import com.sergsave.pocat.models.Card
import com.sergsave.pocat.models.CatData
import com.sergsave.pocat.screens.catcard.analytics.CatCardAnalyticsHelper
import com.sergsave.pocat.sharing.Pack
import com.sergsave.pocat.sharing.WebSharingManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.zipWith
import java.util.concurrent.TimeUnit
import java.io.IOException

class SharingDataExtractViewModel(
    private val sharingManager: WebSharingManager,
    private val contentRepo: ContentRepository,
    private val analytics: CatCardAnalyticsHelper
) : DisposableViewModel() {

    enum class ExtractState {
        INITIAL,
        LOADING,
        NO_CONNECTION_ERROR,
        INVALID_LINK_ERROR,
        UNKNOWN_ERROR
    }

    private val _extractState = MutableLiveData<ExtractState>(ExtractState.INITIAL)
    val extractState: LiveData<ExtractState>
        get() = _extractState

    private val _extractSuccessEvent = MutableLiveData<Event<Card>>()
    val extractSuccessEvent: LiveData<Event<Card>>
        get() = _extractSuccessEvent

    private sealed class Result {
        class Success(val pack: Pack): Result()
        class Error(val throwable: Throwable): Result()
    }

    // Use intent is safe here because we don't save reference to any context.
    fun startExtract(intent: Intent?) {
        if (intent == null) {
            assert(false) { "Need intent!" }
            return
        }

        val download = sharingManager.download(intent)
            .doOnSuccess { analytics.onDownloadFinished(it) }
            .doOnSubscribe { analytics.onDownloadStarted() }
            .doOnDispose { analytics.onDownloadCanceled() }
            .doOnError { analytics.onDownloadFailed(it) }

        val handleError = { throwable: Throwable ->
            val state = when(throwable) {
                is WebSharingManager.NoConnectionException -> ExtractState.NO_CONNECTION_ERROR
                is WebSharingManager.InvalidLinkException -> ExtractState.INVALID_LINK_ERROR
                is IOException -> ExtractState.UNKNOWN_ERROR
                else -> throw throwable
            }
            _extractState.value = state
        }

        _extractState.value = ExtractState.LOADING

        // Allow to avoid progress bar blinking
        val minimumLoadingDurationMillis = 1000L
        val disposable = Single.timer(minimumLoadingDurationMillis, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .zipWith(
                download
                    .map<Result> { Result.Success(it) }
                    .onErrorReturn { Result.Error(it) }
            )
            .subscribe { (_, res) ->
                when (res) {
                    is Result.Success -> updateContent(res.pack.cat)
                    is Result.Error -> handleError(res.throwable)
                }
            }

        addDisposable(disposable)
    }

    private fun updateContent(data: CatData) {
        val disposable = Singles.zip(
                contentRepo.addImage(data.photoUri).onErrorReturnItem(Uri.EMPTY),
                contentRepo.addAudio(data.purrAudioUri).onErrorReturnItem(Uri.EMPTY)
            )
            .subscribe({ (photo, audio) ->
                    if (photo == Uri.EMPTY || audio == Uri.EMPTY) {
                        // TODO: analytics?
                        _extractState.value = ExtractState.INVALID_LINK_ERROR
                    }
                    else {
                        val updated = data.copy(photoUri = photo, purrAudioUri = audio)
                        _extractSuccessEvent.value = Event(Card(null, updated, true, true))
                    }
                }
            )
        addDisposable(disposable)
    }
}
