package com.scrolltax.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scrolltax.app.ui.dashboard.DashboardScreen
import com.scrolltax.app.ui.onboarding.OnboardingScreen
import com.scrolltax.app.ui.settings.SettingsScreen
import com.scrolltax.app.ui.theme.ScrollTaxTheme
import com.scrolltax.domain.CalculateProgressiveTaxUseCase
import com.scrolltax.app.ui.tax.TaxBracketScreen
import com.scrolltax.data.repository.AppUsageRepository
import com.scrolltax.tracking.AppTrackingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appUsageRepository: AppUsageRepository
    @Inject
    lateinit var calculateProgressiveTaxUseCase: CalculateProgressiveTaxUseCase

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndStartTracking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScrollTaxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "onboarding") {
                        composable("onboarding") {
                            OnboardingScreen(
                                onComplete = {
                                    navController.navigate("dashboard") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToTaxBracket = { navController.navigate("tax_brackets") }
                            )
                        }
                        composable("tax_brackets") {
                            TaxBracketScreen(
                                calculateProgressiveTaxUseCase = calculateProgressiveTaxUseCase,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            checkAndStartTracking()
        }
    }

    private fun checkAndStartTracking() {
        if (!appUsageRepository.hasUsageStatsPermission()) {
            // Will be handled in onboarding
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            // Will be handled in onboarding
            return
        }

        startTrackingService()
    }

    private fun startTrackingService() {
        val intent = Intent(this, AppTrackingService::class.java).apply {
            action = AppTrackingService.ACTION_START
        }
        startForegroundService(intent)
    }
}

// Usage Access Activity for redirecting to settings
class UsageAccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
        finish()
    }
}
