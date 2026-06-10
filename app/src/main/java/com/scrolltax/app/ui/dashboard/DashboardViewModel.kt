package com.scrolltax.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrolltax.data.model.DailySummary
import com.scrolltax.data.repository.AnalyticsRepository
import com.scrolltax.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _todaySummary = MutableStateFlow<DailySummary?>(null)
    val todaySummary: StateFlow<DailySummary?> = _todaySummary.asStateFlow()

    private val _trapApps = MutableStateFlow<List<AnalyticsRepository.TrapAppStats>>(emptyList())
    val trapApps: StateFlow<List<AnalyticsRepository.TrapAppStats>> = _trapApps.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _pauseUntil = MutableStateFlow("")
    val pauseUntil: StateFlow<String> = _pauseUntil.asStateFlow()

    private val _last7Days = MutableStateFlow<List<DailySummary>>(emptyList())
    val last7Days: StateFlow<List<DailySummary>> = _last7Days.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _todaySummary.value = analyticsRepository.getOrCreateTodaySummary()
            _trapApps.value = analyticsRepository.getTrapApps()

            analyticsRepository.last7Days.collect { summaries ->
                _last7Days.value = summaries
            }
        }
    }

    fun togglePause() {
        viewModelScope.launch {
            val current = _isPaused.value
            _isPaused.value = !current

            if (!current) {
                // Pausing - set until time
                val until = LocalDateTime.now().plusHours(1)
                _pauseUntil.value = until.format(DateTimeFormatter.ofPattern("h:mm a"))
                settingsRepository.setSilentMode(true)
            } else {
                // Resuming
                _pauseUntil.value = ""
                settingsRepository.setSilentMode(false)
            }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}
