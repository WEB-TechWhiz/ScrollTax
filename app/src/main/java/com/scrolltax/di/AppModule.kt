package com.scrolltax.di

import android.content.Context
import androidx.room.Room
import com.scrolltax.data.db.AppOpenEventDao
import com.scrolltax.data.db.DailySummaryDao
import com.scrolltax.data.db.InterventionEventDao
import com.scrolltax.data.db.ScrollTaxDatabase
import com.scrolltax.data.db.SessionStateDao
import com.scrolltax.data.db.SessionTaxResultDao
import com.scrolltax.data.db.TrackedAppDao
import com.scrolltax.data.db.UserSettingsDao
import com.scrolltax.data.repository.AnalyticsRepository
import com.scrolltax.data.repository.AppUsageRepository
import com.scrolltax.data.repository.InterventionRepository
import com.scrolltax.data.repository.SettingsRepository
import com.scrolltax.data.repository.TrackingRepository
import com.scrolltax.data.repository.TaxBracketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScrollTaxDatabase {
        return Room.databaseBuilder(
            context,
            ScrollTaxDatabase::class.java,
            "scrolltax_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTrackedAppDao(database: ScrollTaxDatabase): TrackedAppDao = database.trackedAppDao()

    @Provides
    fun provideAppOpenEventDao(database: ScrollTaxDatabase): AppOpenEventDao = database.appOpenEventDao()

    @Provides
    fun provideSessionTaxResultDao(database: ScrollTaxDatabase): SessionTaxResultDao = database.sessionTaxResultDao()

    @Provides
    fun provideUserSettingsDao(database: ScrollTaxDatabase): UserSettingsDao = database.userSettingsDao()

    @Provides
    fun provideDailySummaryDao(database: ScrollTaxDatabase): DailySummaryDao = database.dailySummaryDao()

    @Provides
    fun provideInterventionEventDao(database: ScrollTaxDatabase): InterventionEventDao = database.interventionEventDao()

    @Provides
    fun provideSessionStateDao(database: ScrollTaxDatabase): SessionStateDao = database.sessionStateDao()

    @Provides
    @Singleton
    fun provideTrackingRepository(
        trackedAppDao: TrackedAppDao,
        appOpenEventDao: AppOpenEventDao,
        sessionStateDao: SessionStateDao,
        @ApplicationContext context: Context
    ): TrackingRepository {
        return TrackingRepository(trackedAppDao, appOpenEventDao, sessionStateDao, context)
    }

    @Provides
    @Singleton
    fun provideInterventionRepository(
        interventionEventDao: InterventionEventDao,
        sessionTaxResultDao: SessionTaxResultDao,
        sessionStateDao: SessionStateDao,
        @ApplicationContext context: Context
    ): InterventionRepository {
        return InterventionRepository(interventionEventDao, sessionTaxResultDao, sessionStateDao, context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        dailySummaryDao: DailySummaryDao,
        appOpenEventDao: AppOpenEventDao,
        interventionEventDao: InterventionEventDao
    ): AnalyticsRepository {
        return AnalyticsRepository(dailySummaryDao, appOpenEventDao, interventionEventDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        userSettingsDao: UserSettingsDao,
        trackedAppDao: TrackedAppDao
    ): SettingsRepository {
        return SettingsRepository(userSettingsDao, trackedAppDao)
    }

    @Provides
    @Singleton
    fun provideAppUsageRepository(
        @ApplicationContext context: Context
    ): AppUsageRepository {
        return AppUsageRepository(context)

    @Provides
    @Singleton
    fun provideTaxBracketRepository(@ApplicationContext context: Context): TaxBracketRepository {
        return TaxBracketRepository(context)
    }
    }
}
