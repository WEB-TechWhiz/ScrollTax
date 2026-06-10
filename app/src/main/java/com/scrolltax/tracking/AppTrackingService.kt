package com.scrolltax.tracking

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.scrolltax.app.MainActivity
import com.scrolltax.app.R
import com.scrolltax.app.ScrollTaxApplication
import com.scrolltax.data.model.AppCategory
import com.scrolltax.data.model.AppOpenEvent
import com.scrolltax.data.model.InterventionType
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SensitivityMode
import com.scrolltax.data.model.SessionInternalState
import com.scrolltax.data.model.SessionState
import com.scrolltax.data.model.TaxLevel
import com.scrolltax.data.repository.AnalyticsRepository
import com.scrolltax.data.repository.AppUsageRepository
import com.scrolltax.data.repository.InterventionRepository
import com.scrolltax.data.repository.SettingsRepository
import com.scrolltax.data.repository.TrackingRepository
import com.scrolltax.intervention.OverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class AppTrackingService : Service() {

    @Inject
    lateinit var trackingRepository: TrackingRepository

    @Inject
    lateinit var interventionRepository: InterventionRepository

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var appUsageRepository: AppUsageRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null

    private var lastForegroundApp: String? = null
    private var currentSessionId: String? = null
    private var currentEventId: Long? = null
    private var sessionStartTime: Instant? = null
    private var isTracking = false

    companion object {
        const val ACTION_START = "com.scrolltax.tracking.START"
        const val ACTION_STOP = "com.scrolltax.tracking.STOP"
        const val POLLING_INTERVAL_MS = 1000L
        const val MIN_SESSION_DURATION_MS = 2000L
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (isTracking) return
        isTracking = true

        startForeground(
            1,
            createNotification("Tracking your app usage...")
        )

        serviceScope.launch {
            while (isActive && isTracking) {
                try {
                    pollAppUsage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private fun stopTracking() {
        isTracking = false
        currentSessionId?.let { sessionId ->
            serviceScope.launch {
                endCurrentSession(sessionId)
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun pollAppUsage() {
        val currentApp = getCurrentForegroundApp() ?: return
        val settings = settingsRepository.getSettingsSync()

        // Skip if this is our own app or excluded
        if (currentApp == packageName || settings.excludedApps.contains(currentApp)) {
            return
        }

        val isTracked = trackingRepository.isTrackedApp(currentApp)
        val isNight = settingsRepository.isNightTime()

        if (isTracked) {
            if (lastForegroundApp != currentApp) {
                // App changed - handle transition
                lastForegroundApp?.let { prevApp ->
                    if (trackingRepository.isTrackedApp(prevApp)) {
                        endCurrentSession(prevApp)
                    }
                }

                // Start new session
                startNewSession(currentApp, isNight)
            } else {
                // Same app still open - update session
                updateCurrentSession(currentApp)
            }
        } else {
            // Not a tracked app - end any active session
            currentSessionId?.let { endCurrentSession(it) }
        }

        lastForegroundApp = currentApp
    }

    private suspend fun startNewSession(packageName: String, isNight: Boolean) {
        val settings = settingsRepository.getSettingsSync()

        // Record the open event
        val eventId = trackingRepository.recordAppOpen(packageName, isNight)
        currentEventId = eventId

        // Check for recent opens (reopen detection)
        val recentOpens = trackingRepository.getRecentOpens(packageName, 15)
        val isReopen = recentOpens.size > 1
        val isQuickReopen = recentOpens.size > 1 && 
            recentOpens[0].openedAt.isAfter(Instant.now().minus(5, ChronoUnit.MINUTES))

        // Create or get session
        val existingSession = trackingRepository.getActiveSession()

        if (existingSession != null && existingSession.packageName == packageName) {
            // Same app reopened - increment counter
            trackingRepository.incrementOpenCount(existingSession.sessionId)
            currentSessionId = existingSession.sessionId

            // Calculate score
            val score = calculateScore(
                existingSession.copy(openCount = existingSession.openCount + 1),
                isReopen,
                isQuickReopen,
                isNight,
                settings.mode
            )

            // Check intervention threshold
            checkInterventionThreshold(existingSession.sessionId, packageName, score, settings)
        } else {
            // New session
            val session = trackingRepository.createSession(packageName)
            currentSessionId = session.sessionId
            sessionStartTime = Instant.now()

            val score = calculateScore(
                session,
                isReopen,
                isQuickReopen,
                isNight,
                settings.mode
            )

            trackingRepository.updateSessionState(session.sessionId, SessionInternalState.TRACKED_APP_OPENED, score)

            // Small chip for first open if not first time today
            if (recentOpens.isNotEmpty()) {
                showIntervention(session.sessionId, packageName, InterventionType.SMALL_CHIP, score)
            }
        }

        analyticsRepository.recordImpulseOpen()
    }

    private suspend fun updateCurrentSession(packageName: String) {
        val session = currentSessionId?.let { trackingRepository.getActiveSession() } ?: return
        val duration = ChronoUnit.MILLIS.between(sessionStartTime ?: session.lastOpenedAt, Instant.now())

        val settings = settingsRepository.getSettingsSync()
        val score = calculateScore(
            session,
            false,
            false,
            settingsRepository.isNightTime(),
            settings.mode,
            duration
        )

        trackingRepository.updateSessionState(session.sessionId, session.state, score)

        // Check for duration-based interventions
        if (duration > 60000 && score >= 3) { // 1 minute
            checkInterventionThreshold(session.sessionId, packageName, score, settings)
        }
    }

    private suspend fun endCurrentSession(sessionId: String) {
        val session = trackingRepository.getActiveSession() ?: return
        val endTime = Instant.now()
        val duration = ChronoUnit.MILLIS.between(sessionStartTime ?: session.lastOpenedAt, endTime)

        // Update event with close time
        currentEventId?.let { eventId ->
            trackingRepository.updateAppClose(eventId, endTime, duration)
        }

        // Determine if exit was quick (within 10s of intervention)
        val exitWithin10s = session.monkeyTriggeredAt?.let { 
            endTime.isBefore(it.plusSeconds(10)) 
        } ?: false

        if (exitWithin10s) {
            analyticsRepository.recordMonkeyWin()
            val savedMinutes = (duration / 60000).toInt().coerceAtLeast(1)
            analyticsRepository.addSavedMinutes(savedMinutes)
        } else {
            analyticsRepository.recordUserWin()
        }

        // Record session result
        val result = InterventionRepository.SessionResult(
            taxLevel = getTaxLevel(session.score),
            ignoredCount = session.openCount - 1,
            monkeyTriggered = session.monkeyTriggeredAt != null,
            exitWithin10s = exitWithin10s
        )
        interventionRepository.recordSessionResult(sessionId, session.packageName, result)

        trackingRepository.updateSessionState(sessionId, SessionInternalState.SESSION_COMPLETED)

        currentSessionId = null
        currentEventId = null
        sessionStartTime = null
    }

    private fun calculateScore(
        session: SessionState,
        isReopen: Boolean,
        isQuickReopen: Boolean,
        isNight: Boolean,
        sensitivity: SensitivityMode,
        durationMs: Long = 0
    ): Int {
        var score = 0

        // Base open
        score += 1

        // Reopen penalties
        if (isReopen) score += 2
        if (isQuickReopen) score += 3

        // Night usage
        if (isNight) score += 2

        // Duration penalties
        if (durationMs > 60000) score += 2
        if (durationMs > 180000) score += 4

        // Prior ignores
        score += (session.openCount - 1) * 2

        // Apply sensitivity multiplier
        score = when (sensitivity) {
            SensitivityMode.LOW -> (score * 0.7).toInt().coerceAtLeast(1)
            SensitivityMode.MEDIUM -> score
            SensitivityMode.HIGH -> (score * 1.3).toInt()
        }

        return score.coerceAtLeast(1)
    }

    private suspend fun checkInterventionThreshold(
        sessionId: String,
        packageName: String,
        score: Int,
        settings: com.scrolltax.data.model.UserSettings
    ) {
        if (settings.silentMode) return

        val taxLevel = getTaxLevel(score)
        val interventionType = when (taxLevel) {
            TaxLevel.SMALL_CHIP -> InterventionType.SMALL_CHIP
            TaxLevel.STRONG_CHIP -> InterventionType.STRONG_CHIP
            TaxLevel.INTENT_CARD -> InterventionType.INTENT_CARD
            TaxLevel.MONKEY -> {
                if (settings.monkeyEnabled) {
                    InterventionType.MONKEY_BUBBLE
                } else {
                    InterventionType.INTENT_CARD
                }
            }
            else -> return
        }

        showIntervention(sessionId, packageName, interventionType, score)
    }

    private fun getTaxLevel(score: Int): TaxLevel {
        return when {
            score <= 2 -> TaxLevel.SMALL_CHIP
            score <= 4 -> TaxLevel.STRONG_CHIP
            score <= 6 -> TaxLevel.INTENT_CARD
            else -> TaxLevel.MONKEY
        }
    }

    private fun showIntervention(
        sessionId: String,
        packageName: String,
        type: InterventionType,
        score: Int
    ) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SHOW_INTERVENTION
            putExtra(OverlayService.EXTRA_SESSION_ID, sessionId)
            putExtra(OverlayService.EXTRA_PACKAGE_NAME, packageName)
            putExtra(OverlayService.EXTRA_INTERVENTION_TYPE, type.name)
            putExtra(OverlayService.EXTRA_SCORE, score)
        }
        startService(intent)
    }

    private fun getCurrentForegroundApp(): String? {
        return appUsageRepository.getCurrentForegroundApp()
    }

    private fun createNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ScrollTaxApplication.TRACKING_CHANNEL_ID)
            .setContentTitle("Scroll Tax")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ScrollTax::TrackingWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        wakeLock?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
