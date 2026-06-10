package com.scrolltax.tracking

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsHelper @Inject constructor(
    private val context: Context
) {
    fun getUsageStats(startTime: Long, endTime: Long) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
}
