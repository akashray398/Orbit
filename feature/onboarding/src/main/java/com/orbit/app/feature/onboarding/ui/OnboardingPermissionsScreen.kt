package com.orbit.app.feature.onboarding.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.orbit.app.core.designsystem.R
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.components.OrbitButton
import com.orbit.app.core.designsystem.components.OrbitButtonSize
import com.orbit.app.core.designsystem.components.OrbitButtonVariant
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.feature.onboarding.util.PermissionRequester
import com.orbit.app.feature.onboarding.util.PermissionStatus

@Composable
fun OnboardingPermissionsScreen(
    askedPermissions: Set<String>,
    onPermissionAsked: (String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Local reactive map of permission statuses
    val permissionStatuses = remember { mutableStateMapOf<String, PermissionStatus>() }

    fun refreshStatuses() {
        listOf(
            PermissionRequester.KEY_NOTIFICATIONS,
            PermissionRequester.KEY_MICROPHONE,
            PermissionRequester.KEY_CALENDAR,
            PermissionRequester.KEY_NOTIFICATION_ACCESS
        ).forEach { key ->
            val askedBefore = askedPermissions.contains(key)
            permissionStatuses[key] = PermissionRequester.getPermissionStatus(context, key, askedBefore)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshStatuses()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission Launchers
    var activePermissionKeyRequest: String? = null

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        activePermissionKeyRequest?.let { key ->
            onPermissionAsked(key)
            refreshStatuses()
            if (permissionStatuses[key] == PermissionStatus.Granted) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val anyGranted = permissionStatuses.values.any { it == PermissionStatus.Granted }

    OrbitBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OrbitButton(
                        onClick = onContinueClick,
                        variant = OrbitButtonVariant.Primary,
                        size = OrbitButtonSize.Large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (anyGranted) {
                                stringResource(R.string.onboarding_continue)
                            } else {
                                stringResource(R.string.onboarding_skip_for_now)
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.permissions_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.permissions_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Card 1: Notifications
                PermissionCapabilityCard(
                    title = stringResource(R.string.perm_notifications_title),
                    reason = stringResource(R.string.perm_notifications_reason),
                    notDo = stringResource(R.string.perm_notifications_not_do),
                    icon = Icons.Default.Notifications,
                    status = permissionStatuses[PermissionRequester.KEY_NOTIFICATIONS] ?: PermissionStatus.NotAsked,
                    onRequestClick = {
                        activePermissionKeyRequest = PermissionRequester.KEY_NOTIFICATIONS
                        val perms = PermissionRequester.getRuntimePermissions(PermissionRequester.KEY_NOTIFICATIONS)
                        if (perms.isNotEmpty()) {
                            multiplePermissionLauncher.launch(perms)
                        } else {
                            PermissionRequester.openAppSettings(context)
                        }
                    },
                    onOpenSettingsClick = { PermissionRequester.openAppSettings(context) }
                )

                // Card 2: Microphone
                PermissionCapabilityCard(
                    title = stringResource(R.string.perm_microphone_title),
                    reason = stringResource(R.string.perm_microphone_reason),
                    notDo = stringResource(R.string.perm_microphone_not_do),
                    icon = Icons.Default.Mic,
                    status = permissionStatuses[PermissionRequester.KEY_MICROPHONE] ?: PermissionStatus.NotAsked,
                    onRequestClick = {
                        activePermissionKeyRequest = PermissionRequester.KEY_MICROPHONE
                        multiplePermissionLauncher.launch(
                            PermissionRequester.getRuntimePermissions(PermissionRequester.KEY_MICROPHONE)
                        )
                    },
                    onOpenSettingsClick = { PermissionRequester.openAppSettings(context) }
                )

                // Card 3: Calendar
                PermissionCapabilityCard(
                    title = stringResource(R.string.perm_calendar_title),
                    reason = stringResource(R.string.perm_calendar_reason),
                    notDo = stringResource(R.string.perm_calendar_not_do),
                    icon = Icons.Default.CalendarToday,
                    status = permissionStatuses[PermissionRequester.KEY_CALENDAR] ?: PermissionStatus.NotAsked,
                    onRequestClick = {
                        activePermissionKeyRequest = PermissionRequester.KEY_CALENDAR
                        multiplePermissionLauncher.launch(
                            PermissionRequester.getRuntimePermissions(PermissionRequester.KEY_CALENDAR)
                        )
                    },
                    onOpenSettingsClick = { PermissionRequester.openAppSettings(context) }
                )

                // Card 4: Notification Access
                PermissionCapabilityCard(
                    title = stringResource(R.string.perm_notification_access_title),
                    reason = stringResource(R.string.perm_notification_access_reason),
                    notDo = stringResource(R.string.perm_notification_access_not_do),
                    icon = Icons.Default.NotificationsActive,
                    status = permissionStatuses[PermissionRequester.KEY_NOTIFICATION_ACCESS] ?: PermissionStatus.NotAsked,
                    onRequestClick = {
                        onPermissionAsked(PermissionRequester.KEY_NOTIFICATION_ACCESS)
                        PermissionRequester.openNotificationAccessSettings(context)
                    },
                    onOpenSettingsClick = {
                        PermissionRequester.openNotificationAccessSettings(context)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.permissions_later_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PermissionCapabilityCard(
    title: String,
    reason: String,
    notDo: String,
    icon: ImageVector,
    status: PermissionStatus,
    onRequestClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusChip(status = status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Won't do: $notDo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (status != PermissionStatus.Granted) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    when (status) {
                        PermissionStatus.NotAsked -> {
                            OrbitButton(
                                onClick = onRequestClick,
                                variant = OrbitButtonVariant.Secondary,
                                size = OrbitButtonSize.Small
                            ) {
                                Text(stringResource(R.string.perm_action_grant))
                            }
                        }
                        PermissionStatus.Denied -> {
                            OrbitButton(
                                onClick = onRequestClick,
                                variant = OrbitButtonVariant.Secondary,
                                size = OrbitButtonSize.Small
                            ) {
                                Text(stringResource(R.string.perm_action_try_again))
                            }
                        }
                        PermissionStatus.Blocked -> {
                            OrbitButton(
                                onClick = onOpenSettingsClick,
                                variant = OrbitButtonVariant.Secondary,
                                size = OrbitButtonSize.Small
                            ) {
                                Text(stringResource(R.string.perm_action_open_settings))
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: PermissionStatus) {
    val label = when (status) {
        PermissionStatus.NotAsked -> stringResource(R.string.perm_status_not_asked)
        PermissionStatus.Granted -> stringResource(R.string.perm_status_granted)
        PermissionStatus.Denied -> stringResource(R.string.perm_status_denied)
        PermissionStatus.Blocked -> stringResource(R.string.perm_status_blocked)
    }

    OrbitChip(
        label = label,
        selected = status == PermissionStatus.Granted,
        onClick = {},
        leadingIcon = if (status == PermissionStatus.Granted) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else null
    )
}
