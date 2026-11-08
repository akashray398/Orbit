package com.orbit.app.feature.chat.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ChatMessageStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onRetry: (String) -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val accessibilitySender = when (message.role) {
        ChatMessageRole.User -> "You said: "
        ChatMessageRole.Agent -> "Orbit Agent replied: "
        ChatMessageRole.System -> "System notice: "
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .semantics {
                contentDescription = "$accessibilitySender ${message.content}"
            }
    ) {
        when (message.role) {
            ChatMessageRole.User -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .clip(OrbitTheme.shapes.medium)
                            .background(OrbitTheme.colors.primaryGradient)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy") },
                            onClick = {
                                copyToClipboard(context, message.content)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit & Resend") },
                            onClick = {
                                onEdit(message.content)
                                showMenu = false
                            }
                        )
                    }
                }
            }

            ChatMessageRole.Agent -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    AgentOrb(
                        state = AgentState.Idle,
                        size = 32.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        GlassCard(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showMenu = true
                                    }
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val toolLabel = message.actionToolLabel
                                if (toolLabel != null) {
                                    OrbitChip(
                                        label = toolLabel,
                                        selected = true,
                                        onClick = {},
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                val annotatedText = renderMarkdownLight(message.content)
                                Text(
                                    text = if (message.status == ChatMessageStatus.Streaming) {
                                        buildAnnotatedString {
                                            append(annotatedText)
                                            append(" ▌")
                                        }
                                    } else {
                                        annotatedText
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (message.status == ChatMessageStatus.Failed || message.status == ChatMessageStatus.Interrupted) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (message.status == ChatMessageStatus.Interrupted) "Interrupted response" else "Failed to process message",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OrbitTheme.colors.warning
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { onRetry(message.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Retry message",
                                                tint = OrbitTheme.colors.warning
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                onClick = {
                                    copyToClipboard(context, message.content)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            ChatMessageRole.System -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrbitTheme.colors.glassFill)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Orbit Message", text)
    clipboard.setPrimaryClip(clip)
}

/**
 * Basic Markdown-Light renderer supporting bold (**text**), inline code (`code`), and bullet lists (- item).
 */
private fun renderMarkdownLight(input: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = input.split("\n")
        lines.forEachIndexed { index, line ->
            var currentLine = line
            if (currentLine.trimStart().startsWith("- ")) {
                append("• ")
                currentLine = currentLine.trimStart().substring(2)
            }

            // Render inline bold & code tokens
            val parts = currentLine.split("**")
            for (i in parts.indices) {
                if (i % 2 == 1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(parts[i])
                    }
                } else {
                    val codeParts = parts[i].split("`")
                    for (j in codeParts.indices) {
                        if (j % 2 == 1) {
                            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x337C5CFF))) {
                                append(codeParts[j])
                            }
                        } else {
                            append(codeParts[j])
                        }
                    }
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}
