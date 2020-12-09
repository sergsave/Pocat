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

        object UserCardClick {
            const val NAME = "user_card_click"
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

        object AudioSelected {
            const val NAME = "audio_selected"

            object Params {
                const val VALIDATION_RESULT = "validation_result"
            }
        }

        object AudioSelectionStart {
            const val NAME = "audio_selection_start"

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

        object AudioChange {
            const val NAME = "audio_change"
        }

        object PhotoChange {
            const val NAME = "photo_change"
        }

        object TryApplyFormChanges {
            const val NAME = "try_apply_form_changes"

            object Params {
                const val RESULT = "result"
            }
        }

        object CatAdd {
            const val NAME = "cat_add"
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
    }
}