package com.scrolltax.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.scrolltax.data.db.AppOpenEventDao
import com.scrolltax.data.db.DailySummaryDao
import com.scrolltax.data.db.InterventionEventDao
import com.scrolltax.data.db.SessionStateDao
import com.scrolltax.data.db.SessionTaxResultDao
import com.scrolltax.data.db.TrackedAppDao
import com.scrolltax.data.db.UserSettingsDao
import com.scrolltax.data.model.AppCategory
import com.scrolltax.data.model.AppOpenEvent
import com.scrolltax.data.model.DailySummary
import com.scrolltax.data.model.DismissalType
import com.scrolltax.data.model.InterventionEvent
import com.scrolltax.data.model.InterventionType
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SensitivityMode
import com.scrolltax.data.model.SessionInternalState
import com.scrolltax.data.model.SessionState
import com.scrolltax.data.model.SessionTaxResult
import com.scrolltax.data.model.TaxLevel
import com.scrolltax.data.model.TrackedApp
import com.scrolltax.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepository @Inject constructor(
    private val trackedAppDao: TrackedAppDao,
    private val appOpenEventDao: AppOpenEventDao,
    private val sessionStateDao: SessionStateDao,
    private val context: Context
) {
    val trackedApps: Flow<List<TrackedApp>> = trackedAppDao.getAllEnabled()
    val allTrackedApps: Flow<List<TrackedApp>> = trackedAppDao.getAll()

    suspend fun addTrackedApp(packageName: String, displayName: String, category: AppCategory = AppCategory.SOCIAL) {
        trackedAppDao.insert(TrackedApp(packageName, displayName, true, category))
    }

    suspend fun removeTrackedApp(app: TrackedApp) {
        trackedAppDao.delete(app)
    }

    suspend fun toggleAppEnabled(app: TrackedApp) {
        trackedAppDao.update(app.copy(isEnabled = !app.isEnabled))
    }

    suspend fun recordAppOpen(packageName: String, isNight: Boolean = false): Long {
        val event = AppOpenEvent(
            packageName = packageName,
            openedAt = Instant.now(),
            isNightUsage = isNight
        )
        val eventId = appOpenEventDao.insert(event)
        return eventId
    }

    suspend fun updateAppClose(eventId: Long, closedAt: Instant, durationMs: Long) {
        val event = appOpenEventDao.getAll().first().find { it.id == eventId } ?: return
        appOpenEventDao.update(event.copy(closedAt = closedAt, durationMs = durationMs))
    }

    suspend fun createSession(packageName: String): SessionState {
        val sessionId = "${packageName}_${Instant.now().toEpochMilli()}"
        val state = SessionState(
            sessionId = sessionId,
            packageName = packageName,
            state = SessionInternalState.TRACKED_APP_OPENED,
            lastOpenedAt = Instant.now()
        )
        sessionStateDao.insert(state)
        return state
    }

    suspend fun getActiveSession(): SessionState? = sessionStateDao.getActiveSession()

    suspend fun updateSessionState(sessionId: String, newState: SessionInternalState, score: Int? = null) {
        val session = sessionStateDao.getById(sessionId) ?: return
        sessionStateDao.update(session.copy(
            state = newState,
            score = score ?: session.score,
            updatedAt = Instant.now()
        ))
    }

    suspend fun incrementOpenCount(sessionId: String) {
        val session = sessionStateDao.getById(sessionId) ?: return
        sessionStateDao.update(session.copy(
            openCount = session.openCount + 1,
            lastOpenedAt = Instant.now(),
            updatedAt = Instant.now()
        ))
    }

    suspend fun getRecentOpens(packageName: String, minutes: Int = 15): List<AppOpenEvent> {
        val since = Instant.now().minus(minutes.toLong(), ChronoUnit.MINUTES)
        return appOpenEventDao.getRecentByPackage(packageName, since)
    }

    suspend fun isTrackedApp(packageName: String): Boolean {
        return trackedAppDao.getByPackageName(packageName)?.isEnabled == true
    }

    fun getTodayStart(): Instant {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    suspend fun getTodayImpulseCount(): Int {
        return appOpenEventDao.getTodayCount(getTodayStart())
    }

    suspend fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { 
                InstalledAppInfo(
                    packageName = it.packageName,
                    displayName = pm.getApplicationLabel(it).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.displayName }
    }

    data class InstalledAppInfo(
        val packageName: String,
        val displayName: String,
        val icon: android.graphics.drawable.Drawable
    )
}

@Singleton
class InterventionRepository @Inject constructor(
    private val interventionEventDao: InterventionEventDao,
    private val sessionTaxResultDao: SessionTaxResultDao,
    private val sessionStateDao: SessionStateDao,
    private val context: Context
) {
    suspend fun recordIntervention(
        sessionId: String,
        packageName: String,
        type: InterventionType
    ): Long {
        val event = InterventionEvent(
            sessionId = sessionId,
            packageName = packageName,
            interventionType = type,
            shownAt = Instant.now()
        )
        return interventionEventDao.insert(event)
    }

    suspend fun dismissIntervention(
        eventId: Long,
        dismissalType: DismissalType,
        wasEffective: Boolean
    ) {
        val events = interventionEventDao.getSince(Instant.EPOCH).first()
        val event = events.find { it.id == eventId } ?: return
        interventionEventDao.update(event.copy(
            dismissedAt = Instant.now(),
            dismissedBy = dismissalType,
            wasEffective = wasEffective
        ))
    }

    suspend fun recordSessionResult(sessionId: String, packageName: String, result: SessionResult) {
        val taxResult = SessionTaxResult(
            sessionId = sessionId,
            packageName = packageName,
            taxLevel = result.taxLevel,
            ignoredCount = result.ignoredCount,
            monkeyTriggered = result.monkeyTriggered,
            exitWithin10s = result.exitWithin10s
        )
        sessionTaxResultDao.insert(taxResult)
    }

    suspend fun getTodayInterventionCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        return interventionEventDao.getCountSince(todayStart)
    }

    suspend fun getTodayEffectiveCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        return interventionEventDao.getEffectiveCountSince(todayStart)
    }

    data class SessionResult(
        val taxLevel: TaxLevel,
        val ignoredCount: Int = 0,
        val monkeyTriggered: Boolean = false,
        val exitWithin10s: Boolean = false
    )
}

@Singleton
class AnalyticsRepository @Inject constructor(
    private val dailySummaryDao: DailySummaryDao,
    private val appOpenEventDao: AppOpenEventDao,
    private val interventionEventDao: InterventionEventDao
) {
    val last7Days: Flow<List<DailySummary>> = dailySummaryDao.getLast7Days()
    val last30Days: Flow<List<DailySummary>> = dailySummaryDao.getLast30Days()

    suspend fun getOrCreateTodaySummary(): DailySummary {
        val today = LocalDate.now()
        return dailySummaryDao.getByDate(today) ?: DailySummary(date = today).also {
            dailySummaryDao.insert(it)
        }
    }

    suspend fun updateTodaySummary(update: (DailySummary) -> DailySummary) {
        val summary = getOrCreateTodaySummary()
        dailySummaryDao.update(update(summary))
    }

    suspend fun recordImpulseOpen() {
        updateTodaySummary { it.copy(totalImpulseOpens = it.totalImpulseOpens + 1) }
    }

    suspend fun recordInterruptedSession() {
        updateTodaySummary { it.copy(totalInterruptedSessions = it.totalInterruptedSessions + 1) }
    }

    suspend fun recordMonkeyWin() {
        updateTodaySummary { it.copy(monkeyWins = it.monkeyWins + 1) }
    }

    suspend fun recordUserWin() {
        updateTodaySummary { it.copy(userWins = it.userWins + 1) }
    }

    suspend fun addSavedMinutes(minutes: Int) {
        updateTodaySummary { it.copy(totalSavedMinutes = it.totalSavedMinutes + minutes) }
    }

    suspend fun addNightScrollMinutes(minutes: Int) {
        updateTodaySummary { it.copy(nightScrollMinutes = it.nightScrollMinutes + minutes) }
    }

    suspend fun getTrapApps(): List<TrapAppStats> {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val events = appOpenEventDao.getSince(todayStart).first()

        return events
            .groupBy { it.packageName }
            .map { (packageName, opens) ->
                TrapAppStats(
                    packageName = packageName,
                    openCount = opens.size,
                    totalDurationMs = opens.sumOf { it.durationMs },
                    nightOpens = opens.count { it.isNightUsage }
                )
            }
            .sortedByDescending { it.openCount }
    }

    data class TrapAppStats(
        val packageName: String,
        val openCount: Int,
        val totalDurationMs: Long,
        val nightOpens: Int
    )
}

@Singleton
class SettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val trackedAppDao: TrackedAppDao
) {
    val settings: Flow<UserSettings> = userSettingsDao.getSettings()

    suspend fun getSettingsSync(): UserSettings {
        return userSettingsDao.getSettingsSync() ?: UserSettings().also {
            userSettingsDao.insert(it)
        }
    }

    suspend fun updateSettings(update: (UserSettings) -> UserSettings) {
        val current = getSettingsSync()
        userSettingsDao.update(update(current).copy(updatedAt = Instant.now()))
    }

    suspend fun setSensitivity(mode: SensitivityMode) {
        updateSettings { it.copy(mode = mode) }
    }

    suspend fun setBedtime(start: LocalTime, end: LocalTime) {
        updateSettings { it.copy(bedtimeStart = start, bedtimeEnd = end) }
    }

    suspend fun setMonkeyEnabled(enabled: Boolean) {
        updateSettings { it.copy(monkeyEnabled = enabled) }
    }

    suspend fun setMonkeyTone(tone: MonkeyTone) {
        updateSettings { it.copy(monkeyTone = tone) }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        updateSettings { it.copy(reducedMotion = enabled) }
    }

    suspend fun setStrictness(level: Int) {
        updateSettings { it.copy(strictnessLevel = level.coerceIn(1, 5)) }
    }

    suspend fun setSilentMode(enabled: Boolean) {
        updateSettings { it.copy(silentMode = enabled) }
    }

    suspend fun setWeeklyReport(enabled: Boolean) {
        updateSettings { it.copy(weeklyReportEnabled = enabled) }
    }

    suspend fun addExcludedApp(packageName: String) {
        updateSettings { 
            it.copy(excludedApps = it.excludedApps + packageName) 
        }
    }

    suspend fun removeExcludedApp(packageName: String) {
        updateSettings { 
            it.copy(excludedApps = it.excludedApps - packageName) 
        }
    }

    suspend fun isExcluded(packageName: String): Boolean {
        return getSettingsSync().excludedApps.contains(packageName)
    }

    suspend fun isNightTime(): Boolean {
        val settings = getSettingsSync()
        val now = LocalTime.now()
        val start = settings.bedtimeStart
        val end = settings.bedtimeEnd

        return if (start.isBefore(end)) {
            now.isAfter(start) && now.isBefore(end)
        } else {
            now.isAfter(start) || now.isBefore(end)
        }
    }
}

@Singleton
class AppUsageRepository @Inject constructor(
    private val context: Context
) {
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun getUsageStats(startTime: Long, endTime: Long): List<android.app.usage.UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) 
            as android.app.usage.UsageStatsManager
        return usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()
    }

    fun getCurrentForegroundApp(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
            as android.app.usage.UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 // Last minute

        val stats = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return null

        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
