package com.sergsave.purryourcat.screens.catcard.analytics

import android.net.Uri
import com.sergsave.purryourcat.AnalyticsConstants.Events
import com.sergsave.purryourcat.AnalyticsConstants.Events.CatTouch
import com.sergsave.purryourcat.AnalyticsConstants.Events.EditActionClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.SaveActionClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.ShareActionClick
import com.sergsave.purryourcat.AnalyticsConstants.Events.AudioChange
import com.sergsave.purryourcat.AnalyticsConstants.Events.PhotoChange
import com.sergsave.purryourcat.AnalyticsConstants.Events.CatAdd
import com.sergsave.purryourcat.AnalyticsConstants.Events.TryApplyFormChanges
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingTransferParams
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataUpload
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataUploadError
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataDownload
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataDownloadError
import com.sergsave.purryourcat.analytics.AnalyticsEvent

class CatTouch(val durationSec: Long):
    AnalyticsEvent(CatTouch.NAME, mapOf(CatTouch.Params.DURATION to durationSec))

class ShareActionClick: AnalyticsEvent(ShareActionClick.NAME)
class EditActionClick: AnalyticsEvent(EditActionClick.NAME)
class SaveActionClick: AnalyticsEvent(SaveActionClick.NAME)

class TryApplyFormChanges(result: Boolean):
    AnalyticsEvent(TryApplyFormChanges.NAME, mapOf(TryApplyFormChanges.Params.RESULT to result))

class AudioChange: AnalyticsEvent(AudioChange.NAME)
class PhotoChange: AnalyticsEvent(PhotoChange.NAME)

class CatAdd: AnalyticsEvent(CatAdd.NAME)

data class SharingTransferInfo(val durationSec: Long, val photoSize: Long, val audioSize: Long)

private fun makeSharingParams(transferInfo: SharingTransferInfo) = mapOf(
    SharingTransferParams.DURATION to transferInfo.durationSec,
    SharingTransferParams.PHOTO_SIZE to transferInfo.photoSize,
    SharingTransferParams.AUDIO_SIZE to transferInfo.audioSize,
    SharingTransferParams.TOTAL_SIZE to (transferInfo.photoSize + transferInfo.audioSize)
)

class SharingDataUpload(transferInfo: SharingTransferInfo):
    AnalyticsEvent(SharingDataUpload.NAME, makeSharingParams(transferInfo))

class SharingDataDownload(transferInfo: SharingTransferInfo):
    AnalyticsEvent(SharingDataDownload.NAME, makeSharingParams(transferInfo))

enum class SharingError {
    NO_CONNECTION, INVALID_LINK, UNKNOWN;

    override fun toString() = when(this) {
        NO_CONNECTION -> Events.SharingError.NO_CONNECTION
        INVALID_LINK -> Events.SharingError.INVALID_LINK
        UNKNOWN -> Events.SharingError.UNKNOWN
    }
}

class SharingDataUploadError(cause: SharingError):
    AnalyticsEvent(SharingDataUploadError.NAME, mapOf(
        SharingDataUploadError.Params.CAUSE to cause.toString()
    ))

class SharingDataDownloadError(cause: SharingError):
    AnalyticsEvent(SharingDataDownloadError.NAME, mapOf(
        SharingDataDownloadError.Params.CAUSE to cause.toString()
    ))