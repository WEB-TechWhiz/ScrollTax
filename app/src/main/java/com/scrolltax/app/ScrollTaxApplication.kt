package com.scrolltax.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ScrollTaxApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                TRACKING_CHANNEL_ID,
                "App Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Scroll Tax running to track app usage"
                setShowBadge(false)
            }

            val interventionChannel = NotificationChannel(
                INTERVENTION_CHANNEL_ID,
                "Interventions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scroll interventions"
            }

            val summaryChannel = NotificationChannel(
                SUMMARY_CHANNEL_ID,
                "Daily Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily and weekly usage summaries"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(
                trackingChannel, 
                interventionChannel, 
                summaryChannel
            ))
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        const val TRACKING_CHANNEL_ID = "scrolltax_tracking"
        const val INTERVENTION_CHANNEL_ID = "scrolltax_intervention"
        const val SUMMARY_CHANNEL_ID = "scrolltax_summary"
    }
}
