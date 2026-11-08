package com.orbit.app.feature.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.components.OrbitTextField
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.theme.OrbitTheme

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    onMicClick: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = OrbitTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.glassFill)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMicClick,
            modifier = Modifier.semantics { contentDescription = "Voice Input" }
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        OrbitTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Ask Orbit anything...",
            enabled = enabled && !isStreaming,
            singleLine = false,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        BoxButtonContainer(
            isStreaming = isStreaming,
            hasInputText = value.trim().isNotEmpty(),
            enabled = enabled,
            onSendClick = onSendClick,
            onStopClick = onStopClick
        )
    }
}

@Composable
private fun BoxButtonContainer(
    isStreaming: Boolean,
    hasInputText: Boolean,
    enabled: Boolean,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val isSendClickable = enabled && (hasInputText || isStreaming)
    val colors = OrbitTheme.colors

    val bgModifier = when {
        isStreaming -> Modifier.background(colors.danger)
        hasInputText -> Modifier.background(colors.primaryGradient)
        else -> Modifier.background(Color.Transparent)
    }

    IconButton(
        onClick = {
            if (isStreaming) {
                onStopClick()
            } else if (hasInputText) {
                onSendClick()
            }
        },
        enabled = isSendClickable,
        modifier = Modifier
            .size(44.dp)
            .clip(OrbitTheme.shapes.small)
            .then(bgModifier)
            .semantics {
                contentDescription = if (isStreaming) "Stop agent response" else "Send message"
            }
    ) {
        AnimatedContent(
            targetState = isStreaming,
            transitionSpec = { fadeIn(tween(OrbitMotionTokens.DurationFast)) togetherWith fadeOut(tween(OrbitMotionTokens.DurationFast)) },
            label = "sendStopButtonAnimation"
        ) { streaming ->
            if (streaming) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    tint = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (hasInputText) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
