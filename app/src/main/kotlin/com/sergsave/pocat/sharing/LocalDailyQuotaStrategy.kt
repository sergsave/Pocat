package com.sergsave.pocat.sharing

import android.content.Context
import android.content.SharedPreferences
import com.sergsave.pocat.Constants
import java.util.*

// A very simple limitation using a local file.
// User can exceed quota if he deletes application data.
// Also does not protect against changing the system time on the device.
class LocalDailyQuotaStrategy(private val context: Context,
                              private val allowedActionsNumber: Long,
                              uniqueTag: String)
    : DailyQuotaStrategy {

    private val preferences: SharedPreferences
        get() = context.getSharedPreferences(Constants.SHARING_SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE)

    private val startOfCurrentDayMillis: Long
        get() = Calendar.getInstance().run {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

    private val timestampsKey = "${uniqueTag}_timestamps"

    private fun readTimestamps(): List<Long> {
        return preferences.getStringSet(timestampsKey, null)?.map { it.toLong() } ?: emptyList()
    }

    private fun writeTimestamps(timestamps: List<Long>) {
        preferences.edit()
            .putStringSet(timestampsKey, timestamps.map { it.toString() }.toSet())
            .apply()
    }

    override fun canStartAction(): Boolean {
        val todayActionsNumber = readTimestamps().count { it >= startOfCurrentDayMillis }
        return todayActionsNumber < allowedActionsNumber
    }

    override fun onActionFinished() {
        readTimestamps().toMutableList().let {
            it.removeAll { it < startOfCurrentDayMillis }
            it.add(System.currentTimeMillis())
            writeTimestamps(it)
        }
    }
}