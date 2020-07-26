package com.sergsave.purryourcat.sharing

import androidx.annotation.MainThread

class SharingManager private constructor(impl: ISharingStrategy): ISharingStrategy by impl {
    companion object {
        var instance: SharingManager? = null
            private set

        @MainThread
        fun init(impl: ISharingStrategy): SharingManager {
            instance = instance ?: SharingManager(impl)
            return instance!!
        }
    }
}
