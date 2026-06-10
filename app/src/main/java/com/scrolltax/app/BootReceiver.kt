package com.scrolltax.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.scrolltax.data.repository.AppUsageRepository
import com.scrolltax.tracking.AppTrackingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appUsageRepository: AppUsageRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only restart if permissions are already granted
            if (appUsageRepository.hasUsageStatsPermission()) {
                val serviceIntent = Intent(context, AppTrackingService::class.java).apply {
                    action = AppTrackingService.ACTION_START
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
