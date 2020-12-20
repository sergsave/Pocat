package com.sergsave.pocat.screens.catcard.analytics

import com.sergsave.pocat.AnalyticsConstants.Events
import com.sergsave.pocat.AnalyticsConstants.Events.AudioAdded
import com.sergsave.pocat.AnalyticsConstants.Events.AudioAddingError
import com.sergsave.pocat.AnalyticsConstants.Events.NewCatAdded
import com.sergsave.pocat.AnalyticsConstants.Events.CatTouch
import com.sergsave.pocat.AnalyticsConstants.Events.EditActionClick
import com.sergsave.pocat.AnalyticsConstants.Events.PhotoAdded
import com.sergsave.pocat.AnalyticsConstants.Events.PhotoAddingError
import com.sergsave.pocat.AnalyticsConstants.Events.SaveActionClick
import com.sergsave.pocat.AnalyticsConstants.Events.ShareActionClick
import com.sergsave.pocat.AnalyticsConstants.Events.SharingDataDownload
import com.sergsave.pocat.AnalyticsConstants.Events.SharingDataDownloadError
import com.sergsave.pocat.AnalyticsConstants.Events.SharingDataUpload
import com.sergsave.pocat.AnalyticsConstants.Events.SharingDataUploadError
import com.sergsave.pocat.AnalyticsConstants.Events.SharingDataSaveError
import com.sergsave.pocat.AnalyticsConstants.Events.InvalidSharingDataDownloadedError
import com.sergsave.pocat.AnalyticsConstants.Events.SharingTransferParams
import com.sergsave.pocat.AnalyticsConstants.Events.TryApplyFormChanges
import com.sergsave.pocat.AnalyticsConstants.Events.VibrationNotWorkingError
import com.sergsave.pocat.analytics.AnalyticsEvent

class CatTouch(durationSec: Long):
    AnalyticsEvent(CatTouch.NAME, mapOf(CatTouch.Params.DURATION to durationSec))

class ShareActionClick: AnalyticsEvent(ShareActionClick.NAME)
class EditActionClick: AnalyticsEvent(EditActionClick.NAME)
class SaveActionClick: AnalyticsEvent(SaveActionClick.NAME)

class TryApplyFormChanges(result: Boolean):
    AnalyticsEvent(TryApplyFormChanges.NAME, mapOf(TryApplyFormChanges.Params.RESULT to result))

class AudioAdded: AnalyticsEvent(AudioAdded.NAME)
class PhotoAdded: AnalyticsEvent(PhotoAdded.NAME)

class AudioAddingError: AnalyticsEvent(AudioAddingError.NAME)
class PhotoAddingError: AnalyticsEvent(PhotoAddingError.NAME)

class NewCatAdded: AnalyticsEvent(NewCatAdded.NAME)

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

class VibrationNotWorkingError: AnalyticsEvent(VibrationNotWorkingError.NAME)

class InvalidSharingDataDownloadedError: AnalyticsEvent(InvalidSharingDataDownloadedError.NAME)
class SharingDataSaveError: AnalyticsEvent(SharingDataSaveError.NAME)