package com.scrolltax.app.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scrolltax.app.R
import com.scrolltax.app.ui.theme.Background
import com.scrolltax.app.ui.theme.Error
import com.scrolltax.app.ui.theme.Primary
import com.scrolltax.app.ui.theme.Secondary
import com.scrolltax.app.ui.theme.Surface
import com.scrolltax.app.ui.theme.TextSecondary
import com.scrolltax.data.model.MonkeyTone
import com.scrolltax.data.model.SensitivityMode
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tracking Section
            item {
                SettingsSectionHeader(title = "Tracking")
            }

            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Visibility,
                        title = stringResource(R.string.settings_track_apps),
                        subtitle = "${settings?.let { "Configure" } ?: "Configure"} tracked apps",
                        onClick = { /* Navigate to app selection */ }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = Icons.Default.VisibilityOff,
                        title = stringResource(R.string.settings_excluded_apps),
                        subtitle = "Banking, payment, emergency apps",
                        onClick = { /* Navigate to excluded apps */ }
                    )
                }
            }

            // Bedtime Section
            item {
                SettingsSectionHeader(title = "Bedtime & Night")
            }

            item {
                BedtimeSettingsCard(viewModel = viewModel)
            }

            // Monkey Section
            item {
                SettingsSectionHeader(title = "Monkey Interventions")
            }

            item {
                MonkeySettingsCard(viewModel = viewModel)
            }

            // Strictness Section
            item {
                SettingsSectionHeader(title = "Strictness")
            }

            item {
                StrictnessCard(viewModel = viewModel)
            }

            // Notifications Section
            item {
                SettingsSectionHeader(title = "Notifications")
            }

            item {
                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_weekly_report),
                        checked = settings?.weeklyReportEnabled ?: true,
                        onCheckedChange = { viewModel.setWeeklyReport(it) }
                    )

                    SettingsDivider()

                    SettingsToggleItem(
                        icon = Icons.Default.NotificationsOff,
                        title = stringResource(R.string.settings_silent),
                        subtitle = "Disable all intervention sounds",
                        checked = settings?.silentMode ?: false,
                        onCheckedChange = { viewModel.setSilentMode(it) }
                    )
                }
            }

            // Accessibility Section
            item {
                SettingsSectionHeader(title = "Accessibility")
            }

            item {
                SettingsCard {
                    SettingsToggleItem(
                        icon = Icons.Default.MotionPhotosAuto,
                        title = stringResource(R.string.settings_motion),
                        subtitle = "Reduce animations for accessibility",
                        checked = settings?.reducedMotion ?: false,
                        onCheckedChange = { viewModel.setReducedMotion(it) }
                    )
                }
            }

            // Data Section
            item {
                SettingsSectionHeader(title = "Data & Privacy")
            }

            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = stringResource(R.string.settings_data_export),
                        subtitle = "Export your usage data as JSON",
                        onClick = { viewModel.exportData() }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.settings_delete_data),
                        subtitle = "Permanently delete all stored data",
                        titleColor = Error,
                        onClick = { showDeleteDialog = true }
                    )
                }
            }

            // Privacy Info
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All data stays on your device. We never upload or sell your usage patterns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data?") },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = Error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp)
            .height(1.dp)
            .background(TextSecondary.copy(alpha = 0.1f))
    )
}

@Composable
fun BedtimeSettingsCard(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    val bedtimeStart = settings?.bedtimeStart ?: LocalTime.of(22, 0)
    val bedtimeEnd = settings?.bedtimeEnd ?: LocalTime.of(7, 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.settings_bedtime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Start Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.setBedtimeStart(LocalTime.of(hour, minute))
                                },
                                bedtimeStart.hour,
                                bedtimeStart.minute,
                                false
                            ).show()
                        }
                    ) {
                        Text(
                            text = bedtimeStart.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                Text(
                    text = "to",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                // End Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.setBedtimeEnd(LocalTime.of(hour, minute))
                                },
                                bedtimeEnd.hour,
                                bedtimeEnd.minute,
                                false
                            ).show()
                        }
                    ) {
                        Text(
                            text = bedtimeEnd.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            Text(
                text = "Night usage during bedtime hours adds extra tax points",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MonkeySettingsCard(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val monkeyEnabled = settings?.monkeyEnabled ?: true
    val monkeyTone = settings?.monkeyTone ?: com.scrolltax.data.model.MonkeyTone.BALANCED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_monkey),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = monkeyEnabled,
                    onCheckedChange = { viewModel.setMonkeyEnabled(it) }
                )
            }

            if (monkeyEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Monkey Tone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonkeyToneButton(
                        tone = MonkeyTone.FUNNY,
                        label = "Funny",
                        isSelected = monkeyTone == MonkeyTone.FUNNY,
                        onClick = { viewModel.setMonkeyTone(MonkeyTone.FUNNY) },
                        modifier = Modifier.weight(1f)
                    )
                    MonkeyToneButton(
                        tone = MonkeyTone.BALANCED,
                        label = "Balanced",
                        isSelected = monkeyTone == MonkeyTone.BALANCED,
                        onClick = { viewModel.setMonkeyTone(MonkeyTone.BALANCED) },
                        modifier = Modifier.weight(1f)
                    )
                    MonkeyToneButton(
                        tone = MonkeyTone.SAVAGE,
                        label = "Savage",
                        isSelected = monkeyTone == MonkeyTone.SAVAGE,
                        onClick = { viewModel.setMonkeyTone(MonkeyTone.SAVAGE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MonkeyToneButton(
    tone: MonkeyTone,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Primary.copy(alpha = 0.15f) else Primary.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Primary else TextSecondary
        )
    }
}

@Composable
fun StrictnessCard(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val strictness = settings?.strictnessLevel ?: 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.settings_strictness),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = strictness.toFloat(),
                onValueChange = { viewModel.setStrictness(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lenient", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(
                    text = when (strictness) {
                        1 -> "Very Lenient"
                        2 -> "Lenient"
                        3 -> "Balanced"
                        4 -> "Strict"
                        5 -> "Very Strict"
                        else -> "Balanced"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Text("Strict", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
