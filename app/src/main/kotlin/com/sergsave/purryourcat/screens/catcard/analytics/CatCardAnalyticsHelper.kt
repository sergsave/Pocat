package com.sergsave.purryourcat.screens.catcard.analytics

import android.net.Uri
import com.sergsave.purryourcat.analytics.AnalyticsTracker
import com.sergsave.purryourcat.sharing.Pack
import com.sergsave.purryourcat.sharing.WebSharingManager.*
import java.util.concurrent.TimeUnit

private fun diffTimeInSec(startTime: Long) =
    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)

class CatCardAnalyticsHelper(
    private val tracker: AnalyticsTracker,
    private val fileSizeByteCalculator: (Uri) -> Long) {
    private var touchTime: Long? = null
    private var uploadTime: Long? = null
    private var downloadTime: Long? = null

    private var uploadingPack: Pack? = null

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

    fun onChangePhoto() = tracker.sendEvent(PhotoChanged())
    fun onChangeAudio() = tracker.sendEvent(AudioChanged())

    fun onCatAdded() = tracker.sendEvent(CatAdded())

    fun onTryApplyChanges(result: Boolean) = tracker.sendEvent(TryApplyFormChanges(result))

    private fun makeTransferInfo(pack: Pack, transferStartTime: Long) = SharingTransferInfo(
        diffTimeInSec(transferStartTime),
        pack.cat.photoUri?.let { fileSizeByteCalculator(it) } ?: 0,
        pack.cat.purrAudioUri?.let { fileSizeByteCalculator(it) } ?: 0
    )

    fun onUploadStarted(pack: Pack) {
        uploadTime = System.currentTimeMillis()
        uploadingPack = pack
    }

    fun onUploadFinished() {
        val event = uploadTime?.let { time ->
            uploadingPack?.let { SharingDataUploaded(makeTransferInfo(it, time)) }
        }

        event?.let { tracker.sendEvent(it) }
        uploadTime = null
    }

    fun onUploadCanceled() {
        uploadTime = null
    }

    fun onUploadFailed(throwable: Throwable) {
        val cause = when(throwable) {
            is NoConnectionException -> SharingError.NO_CONNECTION
            else -> SharingError.UNKNOWN
        }
        tracker.sendEvent(SharingDataUploadFailed(cause))
    }

    fun onDownloadStarted() {
        downloadTime = System.currentTimeMillis()
    }

    fun onDownloadCanceled() {
        downloadTime = null
    }

    fun onDownloadFinished(pack: Pack) {
        downloadTime?.let {
            tracker.sendEvent(SharingDataDownloaded(makeTransferInfo(pack, it)))
        }
        downloadTime = null
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