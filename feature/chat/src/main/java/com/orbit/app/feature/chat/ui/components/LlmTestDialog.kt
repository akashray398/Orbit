package com.orbit.app.feature.chat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LlmTestDialog(
    testTitle: String,
    streamedText: String,
    toolCallDetails: String?,
    usageText: String?,
    isTesting: Boolean,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(testTitle) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isTesting && streamedText.isEmpty() && toolCallDetails == null) {
                    Text("Connecting to proxy & streaming...", style = MaterialTheme.typography.bodyMedium)
                }

                if (streamedText.isNotEmpty()) {
                    Text(text = "Streamed Output:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = streamedText, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (toolCallDetails != null) {
                    Text(text = "Tool Call Requested:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(text = toolCallDetails, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (usageText != null) {
                    Text(text = usageText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}
