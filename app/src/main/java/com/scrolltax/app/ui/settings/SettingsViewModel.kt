package com.scrolltax.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SensitivityMode
import com.scrolltax.data.repository.AnalyticsRepository
import com.scrolltax.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val settings: StateFlow<com.scrolltax.data.model.UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setBedtimeStart(time: LocalTime) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.setBedtime(time, current.bedtimeEnd)
        }
    }

    fun setBedtimeEnd(time: LocalTime) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsSync()
            settingsRepository.setBedtime(current.bedtimeStart, time)
        }
    }

    fun setMonkeyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMonkeyEnabled(enabled)
        }
    }

    fun setMonkeyTone(tone: MonkeyTone) {
        viewModelScope.launch {
            settingsRepository.setMonkeyTone(tone)
        }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReducedMotion(enabled)
        }
    }

    fun setStrictness(level: Int) {
        viewModelScope.launch {
            settingsRepository.setStrictness(level)
        }
    }

    fun setSilentMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSilentMode(enabled)
        }
    }

    fun setWeeklyReport(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWeeklyReport(enabled)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            // TODO: Implement JSON export to downloads
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            // TODO: Implement data deletion
        }
    }
}
