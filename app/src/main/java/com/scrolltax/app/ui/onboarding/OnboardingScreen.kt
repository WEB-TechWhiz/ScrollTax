package com.scrolltax.app.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scrolltax.app.R
import com.scrolltax.app.ui.theme.Accent
import com.scrolltax.app.ui.theme.Primary
import com.scrolltax.app.ui.theme.PrimaryLight
import com.scrolltax.app.ui.theme.Secondary
import com.scrolltax.app.ui.theme.Success
import com.scrolltax.app.ui.theme.Surface
import com.scrolltax.app.ui.theme.TextSecondary
import com.scrolltax.data.model.AppCategory
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SensitivityMode
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()
    val currentPage by remember { mutableIntStateOf(pagerState.currentPage) }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                totalPages = 6,
                onNext = {
                    if (pagerState.currentPage < 5) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                },
                onBack = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                canProceed = viewModel.canProceed(pagerState.currentPage)
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (page) {
                0 -> WelcomeScreen()
                1 -> PermissionScreen(
                    onRequestUsageAccess = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                    onRequestOverlay = {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    }
                )
                2 -> AppSelectionScreen(viewModel = viewModel)
                3 -> SensitivityScreen(viewModel = viewModel)
                4 -> MonkeySetupScreen(viewModel = viewModel)
                5 -> SuccessScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(PrimaryLight.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PermissionScreen(
    onRequestUsageAccess: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_permission_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_permission_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PermissionItem(
                    icon = Icons.Default.Settings,
                    title = "Usage Access",
                    description = "To detect app opens and track usage patterns",
                    onClick = onRequestUsageAccess
                )

                Spacer(modifier = Modifier.height(16.dp))

                PermissionItem(
                    icon = Icons.Default.Notifications,
                    title = "Overlay Permission",
                    description = "To show intervention nudges over other apps",
                    onClick = onRequestOverlay
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Privacy Promise: All data stays on your device. We never read your messages or content.",
            style = MaterialTheme.typography.bodySmall,
            color = Success,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Button(onClick = onClick) {
            Text("Grant")
        }
    }
}

@Composable
fun AppSelectionScreen(viewModel: OnboardingViewModel) {
    val installedApps by viewModel.installedApps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_app_select_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(R.string.onboarding_app_select_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(installedApps) { app ->
                AppSelectionItem(
                    appName = app.displayName,
                    packageName = app.packageName,
                    isSelected = selectedApps.contains(app.packageName),
                    onToggle = { viewModel.toggleAppSelection(app.packageName) }
                )
            }
        }
    }
}

@Composable
fun AppSelectionItem(
    appName: String,
    packageName: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface
        ),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun SensitivityScreen(viewModel: OnboardingViewModel) {
    val selectedMode by viewModel.sensitivityMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_sensitivity_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.onboarding_sensitivity_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        SensitivityOption(
            mode = SensitivityMode.LOW,
            title = stringResource(R.string.sensitivity_low),
            description = "Minimal interventions, only for repeated opens",
            isSelected = selectedMode == SensitivityMode.LOW,
            onSelect = { viewModel.setSensitivity(SensitivityMode.LOW) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SensitivityOption(
            mode = SensitivityMode.MEDIUM,
            title = stringResource(R.string.sensitivity_medium),
            description = "Balanced approach, moderate nudges",
            isSelected = selectedMode == SensitivityMode.MEDIUM,
            onSelect = { viewModel.setSensitivity(SensitivityMode.MEDIUM) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SensitivityOption(
            mode = SensitivityMode.HIGH,
            title = stringResource(R.string.sensitivity_high),
            description = "Aggressive nudges, frequent interventions",
            isSelected = selectedMode == SensitivityMode.HIGH,
            onSelect = { viewModel.setSensitivity(SensitivityMode.HIGH) }
        )
    }
}

@Composable
fun SensitivityOption(
    mode: SensitivityMode,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun MonkeySetupScreen(viewModel: OnboardingViewModel) {
    val selectedTone by viewModel.monkeyTone.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_monkey_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.onboarding_monkey_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Monkey preview
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐵",
                fontSize = 100.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        MonkeyToneOption(
            tone = MonkeyTone.FUNNY,
            title = stringResource(R.string.monkey_tone_funny),
            isSelected = selectedTone == MonkeyTone.FUNNY,
            onSelect = { viewModel.setMonkeyTone(MonkeyTone.FUNNY) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MonkeyToneOption(
            tone = MonkeyTone.BALANCED,
            title = stringResource(R.string.monkey_tone_balanced),
            isSelected = selectedTone == MonkeyTone.BALANCED,
            onSelect = { viewModel.setMonkeyTone(MonkeyTone.BALANCED) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MonkeyToneOption(
            tone = MonkeyTone.SAVAGE,
            title = stringResource(R.string.monkey_tone_savage),
            isSelected = selectedTone == MonkeyTone.SAVAGE,
            onSelect = { viewModel.setMonkeyTone(MonkeyTone.SAVAGE) }
        )
    }
}

@Composable
fun MonkeyToneOption(
    tone: MonkeyTone,
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SuccessScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Success.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Success
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_success_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_success_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OnboardingBottomBar(
    currentPage: Int,
    totalPages: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    canProceed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentPage + 1) / totalPages.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = Primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }

            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalPages) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) Primary
                                else Primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Button(
                onClick = onNext,
                enabled = canProceed,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    if (currentPage == totalPages - 1) "Get Started" else "Next"
                )
                if (currentPage < totalPages - 1) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
