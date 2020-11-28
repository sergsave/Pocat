package com.sergsave.purryourcat.screens.catcard.analytics

import android.net.Uri
import com.sergsave.purryourcat.AnalyticsConstants.Events
import com.sergsave.purryourcat.AnalyticsConstants.Events.CatTouched
import com.sergsave.purryourcat.AnalyticsConstants.Events.EditActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.SaveActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.ShareActionClicked
import com.sergsave.purryourcat.AnalyticsConstants.Events.NameChanged
import com.sergsave.purryourcat.AnalyticsConstants.Events.AudioChanged
import com.sergsave.purryourcat.AnalyticsConstants.Events.PhotoChanged
import com.sergsave.purryourcat.AnalyticsConstants.Events.TryApplyFormChanges
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataUploaded
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataUploadFailed
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataDownloaded
import com.sergsave.purryourcat.AnalyticsConstants.Events.SharingDataDownloadFailed
import com.sergsave.purryourcat.analytics.AnalyticsEvent

class CatTouched(val duration: Long):
    AnalyticsEvent(CatTouched.NAME, mapOf(CatTouched.Params.DURATION to duration))

class ShareActionClicked: AnalyticsEvent(ShareActionClicked.NAME)
class EditActionClicked: AnalyticsEvent(EditActionClicked.NAME)
class SaveActionClicked: AnalyticsEvent(SaveActionClicked.NAME)

class TryApplyFormChanges(result: Boolean):
    AnalyticsEvent(TryApplyFormChanges.NAME, mapOf(TryApplyFormChanges.Params.RESULT to result))

class NameChanged: AnalyticsEvent(NameChanged.NAME)
class AudioChanged: AnalyticsEvent(AudioChanged.NAME)
class PhotoChanged: AnalyticsEvent(PhotoChanged.NAME)

class SharingDataUploaded(duration: Long, url: Uri):
    AnalyticsEvent(SharingDataUploaded.NAME, mapOf(
        SharingDataUploaded.Params.DURATION to duration,
        SharingDataUploaded.Params.URL to url.toString()
    ))

class SharingDataDownloaded(duration: Long, url: Uri):
    AnalyticsEvent(SharingDataDownloaded.NAME, mapOf(
        SharingDataDownloaded.Params.DURATION to duration,
        SharingDataDownloaded.Params.URL to url.toString()
    ))

enum class SharingError {
    NO_CONNECTION, INVALID_LINK, UNKNOWN;

    override fun toString() = when(this) {
        NO_CONNECTION -> Events.SharingError.NO_CONNECTION
        INVALID_LINK -> Events.SharingError.INVALID_LINK
        UNKNOWN -> Events.SharingError.UNKNOWN
    }
}

class SharingDataUploadFailed(cause: SharingError):
    AnalyticsEvent(SharingDataUploadFailed.NAME, mapOf(
        SharingDataUploadFailed.Params.CAUSE to cause.toString()
    ))

class SharingDataDownloadFailed(cause: SharingError):
    AnalyticsEvent(SharingDataDownloadFailed.NAME, mapOf(
        SharingDataDownloadFailed.Params.CAUSE to cause.toString()
    ))