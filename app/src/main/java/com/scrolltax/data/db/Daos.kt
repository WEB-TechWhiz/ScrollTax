package com.scrolltax.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scrolltax.data.model.AppOpenEvent
import com.scrolltax.data.model.DailySummary
import com.scrolltax.data.model.InterventionEvent
import com.scrolltax.data.model.SessionState
import com.scrolltax.data.model.SessionTaxResult
import com.scrolltax.data.model.TrackedApp
import com.scrolltax.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

@Dao
interface TrackedAppDao {
    @Query("SELECT * FROM tracked_apps WHERE isEnabled = 1")
    fun getAllEnabled(): Flow<List<TrackedApp>>

    @Query("SELECT * FROM tracked_apps")
    fun getAll(): Flow<List<TrackedApp>>

    @Query("SELECT * FROM tracked_apps WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): TrackedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: TrackedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<TrackedApp>)

    @Update
    suspend fun update(app: TrackedApp)

    @Delete
    suspend fun delete(app: TrackedApp)

    @Query("DELETE FROM tracked_apps")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM tracked_apps WHERE isEnabled = 1")
    fun getEnabledCount(): Flow<Int>
}

@Dao
interface AppOpenEventDao {
    @Query("SELECT * FROM app_open_events ORDER BY openedAt DESC")
    fun getAll(): Flow<List<AppOpenEvent>>

    @Query("SELECT * FROM app_open_events WHERE packageName = :packageName ORDER BY openedAt DESC")
    fun getByPackageName(packageName: String): Flow<List<AppOpenEvent>>

    @Query("SELECT * FROM app_open_events WHERE openedAt >= :since ORDER BY openedAt DESC")
    fun getSince(since: Instant): Flow<List<AppOpenEvent>>

    @Query("SELECT COUNT(*) FROM app_open_events WHERE openedAt >= :todayStart")
    suspend fun getTodayCount(todayStart: Instant): Int

    @Query("SELECT * FROM app_open_events WHERE packageName = :packageName AND openedAt >= :since ORDER BY openedAt DESC LIMIT 10")
    suspend fun getRecentByPackage(packageName: String, since: Instant): List<AppOpenEvent>

    @Insert
    suspend fun insert(event: AppOpenEvent): Long

    @Update
    suspend fun update(event: AppOpenEvent)

    @Query("DELETE FROM app_open_events WHERE openedAt < :before")
    suspend fun deleteOlderThan(before: Instant)
}

@Dao
interface SessionTaxResultDao {
    @Query("SELECT * FROM session_tax_results WHERE createdAt >= :since")
    fun getSince(since: Instant): Flow<List<SessionTaxResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: SessionTaxResult)

    @Query("SELECT COUNT(*) FROM session_tax_results WHERE monkeyTriggered = 1 AND createdAt >= :since")
    suspend fun getMonkeyCountSince(since: Instant): Int

    @Query("SELECT COUNT(*) FROM session_tax_results WHERE exitWithin10s = 1 AND createdAt >= :since")
    suspend fun getExitWithin10sCount(since: Instant): Int
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettings>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsSync(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)

    @Update
    suspend fun update(settings: UserSettings)
}

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DailySummary?

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<DailySummary>>

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: DailySummary)

    @Update
    suspend fun update(summary: DailySummary)
}

@Dao
interface InterventionEventDao {
    @Query("SELECT * FROM intervention_events WHERE shownAt >= :since ORDER BY shownAt DESC")
    fun getSince(since: Instant): Flow<List<InterventionEvent>>

    @Query("SELECT COUNT(*) FROM intervention_events WHERE shownAt >= :since")
    suspend fun getCountSince(since: Instant): Int

    @Query("SELECT COUNT(*) FROM intervention_events WHERE wasEffective = 1 AND shownAt >= :since")
    suspend fun getEffectiveCountSince(since: Instant): Int

    @Insert
    suspend fun insert(event: InterventionEvent): Long

    @Update
    suspend fun update(event: InterventionEvent)
}

@Dao
interface SessionStateDao {
    @Query("SELECT * FROM session_states WHERE sessionId = :sessionId")
    suspend fun getById(sessionId: String): SessionState?

    @Query("SELECT * FROM session_states WHERE state != 'SESSION_EXITED' AND state != 'SESSION_COMPLETED' ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getActiveSession(): SessionState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: SessionState)

    @Update
    suspend fun update(state: SessionState)

    @Query("DELETE FROM session_states WHERE updatedAt < :before")
    suspend fun deleteOlderThan(before: Instant)
}
