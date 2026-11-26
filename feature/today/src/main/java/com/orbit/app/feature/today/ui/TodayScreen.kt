package com.orbit.app.feature.today.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onAskClick: () -> Unit,
    onSpeakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val report = uiState.planReport

    LaunchedEffect(uiState.calendarAddedNotice) {
        val notice = uiState.calendarAddedNotice
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    OrbitBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AgentOrb(state = AgentState.Idle, size = 34.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = report?.greeting ?: "Good Day",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = report?.dateStr ?: "Daily Planner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.generateDailyPlan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate Plan", tint = Color(0xFFFF5500))
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Action Buttons (Ask Orbit / Add Plan to Calendar)
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onAskClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5500))
                        ) {
                            Text("Ask Orbit AI")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.addPlanToCalendar() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Calendar")
                        }
                    }
                }

                // 3-Subject Rotated Learning Card
                if (report != null && report.studySubjectsToday.isNotEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF8800), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Today's 3-Subject Learning Focus",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                report.studySubjectsToday.forEachIndexed { idx, subj ->
                                    Text(
                                        text = "${idx + 1}. $subj",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFFF8800),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Header for Timeline
                item {
                    Text(
                        text = "Daily Routine & Study Plan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                // List of Daily Plan Items
                val itemsList = report?.items ?: emptyList()
                items(
                    items = itemsList,
                    key = { it.id }
                ) { item ->
                    val categoryColor = when (item.category) {
                        "HEALTH" -> Color(0xFF10B981)
                        "STUDY" -> Color(0xFFFF8800)
                        "PROJECT" -> Color(0xFFFF5500)
                        "AI_LEARNING" -> Color(0xFF3B82F6)
                        else -> Color(0xFFA89F9B)
                    }

                    val categoryIcon = when (item.category) {
                        "STUDY" -> Icons.Default.Star
                        "PROJECT" -> Icons.Default.Build
                        "AI_LEARNING" -> Icons.Default.Info
                        else -> Icons.Default.Info
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleItemCompleted(item.id) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (item.isCompleted) Color(0xFF10B981) else categoryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isCompleted) Icons.Default.Check else categoryIcon,
                                    contentDescription = null,
                                    tint = if (item.isCompleted) Color.White else categoryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.timeStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = categoryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${item.durationMinutes}m)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
