package com.sergsave.pocat

object AnalyticsConstants {
    object Events {
        object AppStart {
            const val NAME = "app_start"
        }

        object TabOpen {
            const val NAME = "tab_open"

            object TabType {
                const val SAMPLES = "samples"
                const val USERS = "users"
            }

            object Params {
                const val TAB = "tab"
            }
        }

        object UsersCatsListUpdate {
            const val NAME = "users_cats_list_update"

            object Params {
                const val COUNT = "count"
            }
        }

        object UsersCatCardClick {
            const val NAME = "users_cat_card_click"
        }

        object SampleCardClick {
            const val NAME = "sample_card_click"

            object Params {
                const val ID = "id"
            }
        }

        object CatsRemove {
            const val NAME = "cats_remove"

            object Params {
                const val COUNT = "count"
            }
        }

        object AddButtonClick {
            const val NAME = "add_button_click"
        }

        object SettingsActionClick {
            const val NAME = "settings_action_click"
        }

        object DonateActionClick {
            const val NAME = "donate_action_click"
        }

        object AboutActionClick {
            const val NAME = "about_action_click"
        }

        object VibrationSwitch {
            const val NAME = "vibration_switch"

            object Params {
                const val STATE = "state"
            }
        }

        object AudioFromSamplesClick {
            const val NAME = "audio_from_samples_click"
        }

        object AudioFromRecorderClick {
            const val NAME = "audio_from_recorder_click"
        }

        object AudioFromMemoryClick {
            const val NAME = "audio_from_memory_click"
        }

        object AudioSizeError {
            const val NAME = "audio_size_error"
        }

        object RecorderNotFound {
            const val NAME = "recorder_not_found"
        }

        object CatTouch {
            const val NAME = "cat_touch"

            object Params {
                const val DURATION = "duration"
            }
        }

        object ShareActionClick {
            const val NAME = "share_action_click"
        }

        object SaveActionClick {
            const val NAME = "save_action_click"
        }

        object EditActionClick {
            const val NAME = "edit_action_click"
        }

        object AudioAdded {
            const val NAME = "audio_added"
        }

        object AudioAddingError {
            const val NAME = "audio_adding_error"
        }

        object PhotoAdded {
            const val NAME = "photo_added"
        }

        object PhotoAddingError {
            const val NAME = "photo_adding_error"
        }

        object TryApplyFormChanges {
            const val NAME = "try_apply_form_changes"

            object Params {
                const val RESULT = "result"
            }
        }

        object NewCatAdded {
            const val NAME = "new_cat_added"
        }

        object SharingTransferParams {
            const val DURATION = "duration"
            const val PHOTO_SIZE = "photo_size"
            const val AUDIO_SIZE = "audio_size"
            const val TOTAL_SIZE = "total_size"
        }

        object SharingDataUpload {
            const val NAME = "sharing_data_upload"
        }

        object SharingDataDownload {
            const val NAME = "sharing_data_download"
        }

        object SharingError {
            const val NO_CONNECTION = "no_connection"
            const val INVALID_LINK = "invalid_link"
            const val QUOTA_EXCEEDED = "quota_exceeded"
            const val UNKNOWN = "unknown"
        }

        object SharingDataUploadError {
            const val NAME = "sharing_data_upload_error"

            object Params {
                const val CAUSE = "cause"
            }
        }

        object SharingDataDownloadError {
            const val NAME = "sharing_data_download_error"

            object Params {
                const val CAUSE = "cause"
            }
        }

        // TODO: remove WTF errors
        object InvalidSharingDataDownloadedError {
            const val NAME = "invalid_sharing_data_downloaded_error"
        }

        object SharingDataSaveError {
            const val NAME = "sharing_data_save_error"
        }

        object VibrationNotWorkingError {
            const val NAME = "vibration_not_working_error"
        }

        object CatsRemovingError {
            const val NAME = "cats_removing_error"
        }

        object CleanupError {
            const val NAME = "cleanup_error"
        }
    }
}