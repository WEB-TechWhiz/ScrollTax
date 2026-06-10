package com.scrolltax.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrolltax.data.model.AppCategory
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SensitivityMode
import com.scrolltax.data.model.TrackedApp
import com.scrolltax.data.repository.SettingsRepository
import com.scrolltax.data.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<TrackingRepository.InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<TrackingRepository.InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _sensitivityMode = MutableStateFlow(SensitivityMode.MEDIUM)
    val sensitivityMode: StateFlow<SensitivityMode> = _sensitivityMode.asStateFlow()

    private val _monkeyTone = MutableStateFlow(MonkeyTone.BALANCED)
    val monkeyTone: StateFlow<MonkeyTone> = _monkeyTone.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    private val _hasOverlayPermission = MutableStateFlow(false)

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = trackingRepository.getInstalledApps()
            _installedApps.value = apps
        }
    }

    fun toggleAppSelection(packageName: String) {
        _selectedApps.value = _selectedApps.value.toMutableSet().apply {
            if (contains(packageName)) remove(packageName) else add(packageName)
        }
    }

    fun setSensitivity(mode: SensitivityMode) {
        _sensitivityMode.value = mode
    }

    fun setMonkeyTone(tone: MonkeyTone) {
        _monkeyTone.value = tone
    }

    fun canProceed(page: Int): Boolean {
        return when (page) {
            0 -> true // Welcome
            1 -> _hasUsagePermission.value && _hasOverlayPermission.value
            2 -> _selectedApps.value.isNotEmpty()
            3 -> true // Sensitivity always selected
            4 -> true // Monkey tone always selected
            5 -> true // Success
            else -> false
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // Save selected apps
            _selectedApps.value.forEach { packageName ->
                val appInfo = _installedApps.value.find { it.packageName == packageName }
                appInfo?.let {
                    trackingRepository.addTrackedApp(
                        packageName = it.packageName,
                        displayName = it.displayName,
                        category = AppCategory.SOCIAL
                    )
                }
            }

            // Save settings
            settingsRepository.setSensitivity(_sensitivityMode.value)
            settingsRepository.setMonkeyTone(_monkeyTone.value)
            settingsRepository.setMonkeyEnabled(true)
        }
    }
}
