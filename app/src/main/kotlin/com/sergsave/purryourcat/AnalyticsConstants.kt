package com.sergsave.purryourcat

object AnalyticsConstants {
    object Events {
        object TabSwitched {
            const val NAME = "tab_switched"

            object Params {
                const val TAB_TYPE = "tab"
            }

            object TabType {
                const val SAMPLES = "samples"
                const val USERS = "users"
            }
        }
    }
}