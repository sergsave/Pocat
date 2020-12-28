package com.sergsave.pocat.sharing

interface DailyQuotaStrategy {
    fun canStartAction(): Boolean
    fun onActionFinished()
}