package com.scrolltax.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tracked_apps")
data class TrackedApp(
    @PrimaryKey
    val packageName: String,
    val displayName: String,
    val isEnabled: Boolean = true,
    val category: AppCategory = AppCategory.SOCIAL,
    val addedAt: Instant = Instant.now()
)

enum class AppCategory {
    SOCIAL, ENTERTAINMENT, NEWS, SHOPPING, GAMING, OTHER
}

@Entity(tableName = "app_open_events")
data class AppOpenEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val openedAt: Instant,
    val closedAt: Instant? = null,
    val durationMs: Long = 0,
    val isNightUsage: Boolean = false,
    val sessionScore: Int = 0
)

@Entity(tableName = "session_tax_results")
data class SessionTaxResult(
    @PrimaryKey
    val sessionId: String,
    val packageName: String,
    val taxLevel: TaxLevel,
    val ignoredCount: Int = 0,
    val monkeyTriggered: Boolean = false,
    val exitWithin10s: Boolean = false,
    val createdAt: Instant = Instant.now()
)

enum class TaxLevel {
    NONE, SMALL_CHIP, STRONG_CHIP, INTENT_CARD, MONKEY
}

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val mode: SensitivityMode = SensitivityMode.MEDIUM,
    val bedtimeStart: LocalTime = LocalTime.of(22, 0),
    val bedtimeEnd: LocalTime = LocalTime.of(7, 0),
    val monkeyEnabled: Boolean = true,
    val monkeyTone: MonkeyTone = MonkeyTone.BALANCED,
    val reducedMotion: Boolean = false,
    val strictnessLevel: Int = 3,
    val silentMode: Boolean = false,
    val weeklyReportEnabled: Boolean = true,
    val excludedApps: List<String> = emptyList(),
    val updatedAt: Instant = Instant.now()
)

enum class SensitivityMode {
    LOW, MEDIUM, HIGH
}

enum class MonkeyTone {
    FUNNY, BALANCED, SAVAGE
}

@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey
    val date: LocalDate,
    val totalImpulseOpens: Int = 0,
    val totalInterruptedSessions: Int = 0,
    val monkeyWins: Int = 0,
    val userWins: Int = 0,
    val totalSavedMinutes: Int = 0,
    val nightScrollMinutes: Int = 0,
    val createdAt: Instant = Instant.now()
)

@Entity(tableName = "intervention_events")
data class InterventionEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val packageName: String,
    val interventionType: InterventionType,
    val shownAt: Instant,
    val dismissedAt: Instant? = null,
    val dismissedBy: DismissalType? = null,
    val wasEffective: Boolean = false
)

enum class InterventionType {
    SMALL_CHIP, STRONG_CHIP, INTENT_CARD, MONKEY_BUBBLE, MONKEY_ANGRY
}

enum class DismissalType {
    EXIT_APP, CONTINUE, IGNORE, TIMEOUT
}

@Entity(tableName = "session_states")
data class SessionState(
    @PrimaryKey
    val sessionId: String,
    val packageName: String,
    val state: SessionInternalState,
    val score: Int = 0,
    val openCount: Int = 1,
    val lastOpenedAt: Instant,
    val warningShownAt: Instant? = null,
    val monkeyTriggeredAt: Instant? = null,
    val exitedAt: Instant? = null,
    val updatedAt: Instant = Instant.now()
)

enum class SessionInternalState {
    IDLE, TRACKED_APP_OPENED, WARNING_SHOWN, WARNING_IGNORED, 
    MONKEY_TRIGGERED, SESSION_EXITED, SESSION_COMPLETED
}

// Type Converters for Room
class Converters {
    @androidx.room.TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @androidx.room.TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @androidx.room.TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @androidx.room.TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @androidx.room.TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @androidx.room.TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @androidx.room.TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString(",")

    @androidx.room.TypeConverter
    fun toStringList(value: String?): List<String> = value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()

    @androidx.room.TypeConverter
    fun fromTaxLevel(value: TaxLevel?): String? = value?.name

    @androidx.room.TypeConverter
    fun toTaxLevel(value: String?): TaxLevel? = value?.let { TaxLevel.valueOf(it) }

    @androidx.room.TypeConverter
    fun fromSessionState(value: SessionInternalState?): String? = value?.name

    @androidx.room.TypeConverter
    fun toSessionState(value: String?): SessionInternalState? = value?.let { SessionInternalState.valueOf(it) }

    @androidx.room.TypeConverter
    fun fromInterventionType(value: InterventionType?): String? = value?.name

    @androidx.room.TypeConverter
    fun toInterventionType(value: String?): InterventionType? = value?.let { InterventionType.valueOf(it) }

    @androidx.room.TypeConverter
    fun fromDismissalType(value: DismissalType?): String? = value?.name

    @androidx.room.TypeConverter
    fun toDismissalType(value: String?): DismissalType? = value?.let { DismissalType.valueOf(it) }

    @androidx.room.TypeConverter
    fun fromAppCategory(value: AppCategory?): String? = value?.name

    @androidx.room.TypeConverter
    fun toAppCategory(value: String?): AppCategory? = value?.let { AppCategory.valueOf(it) }

    @androidx.room.TypeConverter
    fun fromSensitivityMode(value: SensitivityMode?): String? = value?.name

    @androidx.room.TypeConverter
    fun toSensitivityMode(value: String?): SensitivityMode? = value?.let { SensitivityMode.valueOf(it) }

    @androidx.room.TypeConverter
    fun fromMonkeyTone(value: MonkeyTone?): String? = value?.name

    @androidx.room.TypeConverter
    fun toMonkeyTone(value: String?): MonkeyTone? = value?.let { MonkeyTone.valueOf(it) }
}
