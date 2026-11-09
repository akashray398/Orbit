package com.orbit.app.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.ReducedMotionOverride
import com.orbit.app.core.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messageText) {
        val msg = uiState.messageText
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    OrbitBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Settings & Privacy") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Section 1: Agent Capabilities & Tools
                Text("What Orbit Can Do", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                uiState.tools.forEach { tool ->
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(tool.name, style = MaterialTheme.typography.titleSmall)
                            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Data & Privacy Sources
                Text("What Orbit Can Read", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Calendar Access: ${if (uiState.hasCalendarPermission) "Granted" else "Prefilled Intent Mode"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Captured Notifications: ${uiState.capturedNotificationsCount} stored", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Data Maintenance
                Text("Data Maintenance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = viewModel::clearChatHistory, modifier = Modifier.weight(1f)) {
                        Text("Clear Chat")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = viewModel::deleteAllNotes, modifier = Modifier.weight(1f)) {
                        Text("Delete Notes")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = viewModel::deleteAllTasksAndReminders, modifier = Modifier.weight(1f)) {
                        Text("Delete Tasks")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = viewModel::wipeAllData, modifier = Modifier.weight(1f)) {
                        Text("Wipe All")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Appearance & Motion
                Text("Appearance & Theme", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Theme Mode", modifier = Modifier.weight(1f))
                            Button(onClick = {
                                val next = when (uiState.appSettings.themeMode) {
                                    ThemeMode.System -> ThemeMode.Dark
                                    ThemeMode.Dark -> ThemeMode.Light
                                    ThemeMode.Light -> ThemeMode.System
                                }
                                viewModel.setThemeMode(next)
                            }) {
                                Text(uiState.appSettings.themeMode.name)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reduced Motion", modifier = Modifier.weight(1f))
                            Button(onClick = {
                                val next = when (uiState.appSettings.reducedMotionOverride) {
                                    ReducedMotionOverride.System -> ReducedMotionOverride.On
                                    ReducedMotionOverride.On -> ReducedMotionOverride.Off
                                    ReducedMotionOverride.Off -> ReducedMotionOverride.System
                                }
                                viewModel.setReducedMotion(next)
                            }) {
                                Text(uiState.appSettings.reducedMotionOverride.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Section 5: Debug
                Text("Debug Options", style = MaterialTheme.typography.titleMedium, color = OrbitTheme.colors.warning)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = viewModel::resetOnboarding, modifier = Modifier.fillMaxWidth()) {
                    Text("Reset Onboarding Status")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
