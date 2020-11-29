package com.sergsave.purryourcat

object AnalyticsConstants {
    object Events {
        object AppStarted {
            const val NAME = "app_started"
        }

        object TabOpened {
            const val NAME = "tab_opened"

            object TabType {
                const val SAMPLES = "samples"
                const val USERS = "users"
            }

            object Params {
                const val TAB = "tab"
            }
        }

        object UserCardClicked {
            const val NAME = "user_card_clicked"
        }

        object SampleCardClicked {
            const val NAME = "sample_card_clicked"

            object Params {
                const val ID = "id"
            }
        }

        object CatsRemoved {
            const val NAME = "cats_removed"

            object Params {
                const val COUNT = "count"
            }
        }

        object AddButtonClicked {
            const val NAME = "add_button_clicked"
        }

        object SettingsActionClicked {
            const val NAME = "settings_action_clicked"
        }

        object DonateActionClicked {
            const val NAME = "donate_action_clicked"
        }

        object AboutActionClicked {
            const val NAME = "about_action_clicked"
        }

        object VibrationSwitched {
            const val NAME = "vibration_switched"

            object Params {
                const val STATE = "state"
            }
        }

        object AudioSelected {
            const val NAME = "audio_selected"

            object Params {
                const val VALIDATION_RESULT = "validation_result"
            }
        }

        object AddAudioRequested {
            const val NAME = "add_audio_requested"

            object Params {
                const val SOURCE = "source"
            }

            object Source {
                const val SAMPLES = "samples"
                const val RECORDER = "recorder"
                const val DEVICE = "device"
            }
        }

        object RecorderNotFound {
            const val NAME = "recorder_not_found"
        }

        object CatTouched {
            const val NAME = "cat_touched"

            object Params {
                const val DURATION = "duration"
            }
        }

        object ShareActionClicked {
            const val NAME = "share_action_clicked"
        }

        object SaveActionClicked {
            const val NAME = "save_action_clicked"
        }

        object EditActionClicked {
            const val NAME = "edit_action_clicked"
        }

        object AudioChanged {
            const val NAME = "audio_changed"
        }

        object PhotoChanged {
            const val NAME = "photo_changed"
        }

        object TryApplyFormChanges {
            const val NAME = "try_apply_form_changes"

            object Params {
                const val RESULT = "result"
            }
        }

        object CatAdded {
            const val NAME = "cat_added"
        }

        object SharingTransferParams {
            const val DURATION = "duration"
            const val PHOTO_SIZE = "photo_size"
            const val AUDIO_SIZE = "audio_size"
            const val TOTAL_SIZE = "total_size"
        }

        object SharingDataUploaded {
            const val NAME = "sharing_data_uploaded"
        }

        object SharingDataDownloaded {
            const val NAME = "sharing_data_downloaded"
        }

        object SharingError {
            const val NO_CONNECTION = "no_connection"
            const val INVALID_LINK = "invalid_link"
            const val UNKNOWN = "unknown"
        }

        object SharingDataUploadFailed {
            const val NAME = "sharing_data_upload_failed"

            object Params {
                const val CAUSE = "cause"
            }
        }

        object SharingDataDownloadFailed {
            const val NAME = "sharing_data_download_failed"

            object Params {
                const val CAUSE = "cause"
            }
        }
    }
}