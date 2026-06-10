package com.scrolltax.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scrolltax.app.R
import com.scrolltax.app.ui.theme.Accent
import com.scrolltax.app.ui.theme.Background
import com.scrolltax.app.ui.theme.Error
import com.scrolltax.app.ui.theme.Primary
import com.scrolltax.app.ui.theme.PrimaryLight
import com.scrolltax.app.ui.theme.Secondary
import com.scrolltax.app.ui.theme.Success
import com.scrolltax.app.ui.theme.Surface
import com.scrolltax.app.ui.theme.TextSecondary
import com.scrolltax.app.ui.theme.Warning
import com.scrolltax.data.model.DailySummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val todaySummary by viewModel.todaySummary.collectAsState()
    val trapApps by viewModel.trapApps.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val last7Days by viewModel.last7Days.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.togglePause() },
                containerColor = if (isPaused) Success else Warning,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause"
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pause indicator
            if (isPaused) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = Warning
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Nudges paused until ${viewModel.pauseUntil.value}",
                                color = Warning,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Stats Grid
            item {
                StatsGrid(summary = todaySummary)
            }

            // Trap Apps
            item {
                TrapAppsSection(trapApps = trapApps)
            }

            // Night Scroll Report
            item {
                NightScrollCard(minutes = todaySummary?.nightScrollMinutes ?: 0)
            }

            // Weekly Trend
            if (last7Days.isNotEmpty()) {
                item {
                    WeeklyTrendCard(summaries = last7Days)
                }
            }

            // Quick Actions
            item {
                QuickActionsSection(
                    onPauseClicked = { viewModel.togglePause() },
                    isPaused = isPaused
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun StatsGrid(summary: DailySummary?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.impulse_opens),
                value = summary?.totalImpulseOpens?.toString() ?: "0",
                icon = Icons.Default.TrendingUp,
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.monkey_interventions),
                value = summary?.totalInterruptedSessions?.toString() ?: "0",
                icon = Icons.Default.Notifications,
                color = Secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.saved_minutes),
                value = "${summary?.totalSavedMinutes ?: 0}m",
                icon = Icons.Default.TrendingDown,
                color = Success,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Monkey Wins",
                value = summary?.monkeyWins?.toString() ?: "0",
                icon = Icons.Default.TrendingDown,
                color = Accent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun TrapAppsSection(trapApps: List<com.scrolltax.data.repository.AnalyticsRepository.TrapAppStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.trap_apps),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (trapApps.isEmpty()) {
                Text(
                    text = "No trap apps detected today. Great job staying mindful!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                trapApps.take(5).forEach { app ->
                    TrapAppItem(app = app)
                    if (app != trapApps.take(5).last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TrapAppItem(app: com.scrolltax.data.repository.AnalyticsRepository.TrapAppStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.packageName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${app.openCount} opens today",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${app.totalDurationMs / 60000}m",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (app.totalDurationMs > 300000) Error else Primary
            )
            if (app.nightOpens > 0) {
                Text(
                    text = "${app.nightOpens} night opens",
                    style = MaterialTheme.typography.labelSmall,
                    color = Error
                )
            }
        }
    }
}

@Composable
fun NightScrollCard(minutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (minutes > 30) Error.copy(alpha = 0.1f) else Surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (minutes > 30) Error.copy(alpha = 0.15f) else Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (minutes > 30) Error else Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.night_scroll),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (minutes > 0) "${minutes}m of late-night scrolling" else "No late-night scrolling detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (minutes > 30) {
                Text(
                    text = "⚠️",
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun WeeklyTrendCard(summaries: List<DailySummary>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "7-Day Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple bar chart visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxValue = summaries.maxOfOrNull { it.totalImpulseOpens }?.coerceAtLeast(1) ?: 1

                summaries.reversed().forEach { summary ->
                    val height = (summary.totalImpulseOpens.toFloat() / maxValue).coerceIn(0f, 1f)
                    val animatedHeight by animateFloatAsState(
                        targetValue = height,
                        label = "bar_height"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((animatedHeight * 100).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (summary.totalImpulseOpens > summaries.map { it.totalImpulseOpens }.average()) 
                                        Error.copy(alpha = 0.7f) 
                                    else Primary.copy(alpha = 0.7f)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.date.format(DateTimeFormatter.ofPattern("EEE")),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onPauseClicked: () -> Unit,
    isPaused: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPauseClicked,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) Success else Warning
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isPaused) "Resume Nudges" else stringResource(R.string.pause_nudges)
                )
            }
        }
    }
}
