package com.sergsave.purryourcat.screens.catcard.analytics

import android.net.Uri
import com.sergsave.purryourcat.analytics.AnalyticsTracker
import com.sergsave.purryourcat.sharing.WebSharingManager.*
import java.util.concurrent.TimeUnit

private fun diffTimeInSec(startTime: Long) =
    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)

class CatCardAnalyticsHelper(private val tracker: AnalyticsTracker) {
    private var touchTime: Long? = null
    private var uploadTime: Long? = null
    private var downloadTime: Long? = null
    private var downloadLink: Uri? = null

    fun onTouchStarted() {
        touchTime = System.currentTimeMillis()
    }

    fun onTouchFinished() {
        touchTime?.let { tracker.sendEvent(CatTouched(diffTimeInSec(it))) }
        touchTime = null
    }

    fun onShareClicked() = tracker.sendEvent(ShareActionClicked())
    fun onEditClicked() = tracker.sendEvent(EditActionClicked())
    fun onSaveClicked() = tracker.sendEvent(SaveActionClicked())

    fun onChangeName() = tracker.sendEvent(NameChanged())
    fun onChangePhoto() = tracker.sendEvent(PhotoChanged())
    fun onChangeAudio() = tracker.sendEvent(AudioChanged())

    fun onTryApplyChanges(result: Boolean) = tracker.sendEvent(TryApplyFormChanges(result))

    fun onUploadStarted() {
        uploadTime = System.currentTimeMillis()
    }

    fun onUploadFinished(uri: Uri) {
        uploadTime?.let { tracker.sendEvent(SharingDataUploaded(diffTimeInSec(it), uri)) }
        uploadTime = null
    }

    fun onUploadFailed(throwable: Throwable) {
        val cause = when(throwable) {
            is NoConnectionException -> SharingError.NO_CONNECTION
            else -> SharingError.UNKNOWN
        }
        tracker.sendEvent(SharingDataUploadFailed(cause))
    }

    fun onDownloadStarted(uri: Uri) {
        downloadTime = System.currentTimeMillis()
        downloadLink = uri
    }

    fun onDownloadFinished() {
        val uri = downloadLink
        if (uri == null)
            return

        downloadTime?.let { tracker.sendEvent(SharingDataDownloaded(diffTimeInSec(it), uri)) }
        downloadTime = null
        downloadLink = null
    }

    fun onDownloadFailed(throwable: Throwable) {
        val cause = when(throwable) {
            is NoConnectionException -> SharingError.NO_CONNECTION
            is InvalidLinkException -> SharingError.INVALID_LINK
            else -> SharingError.UNKNOWN
        }
        tracker.sendEvent(SharingDataDownloadFailed(cause))
    }
}