package com.scrolltax.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scrolltax.data.model.AppOpenEvent
import com.scrolltax.data.model.Converters
import com.scrolltax.data.model.DailySummary
import com.scrolltax.data.model.InterventionEvent
import com.scrolltax.data.model.SessionState
import com.scrolltax.data.model.SessionTaxResult
import com.scrolltax.data.model.TrackedApp
import com.scrolltax.data.model.UserSettings

@Database(
    entities = [
        TrackedApp::class,
        AppOpenEvent::class,
        SessionTaxResult::class,
        UserSettings::class,
        DailySummary::class,
        InterventionEvent::class,
        SessionState::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScrollTaxDatabase : RoomDatabase() {
    abstract fun trackedAppDao(): TrackedAppDao
    abstract fun appOpenEventDao(): AppOpenEventDao
    abstract fun sessionTaxResultDao(): SessionTaxResultDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun interventionEventDao(): InterventionEventDao
    abstract fun sessionStateDao(): SessionStateDao
}
