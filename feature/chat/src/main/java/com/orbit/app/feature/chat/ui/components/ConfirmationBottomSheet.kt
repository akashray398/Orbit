package com.orbit.app.feature.chat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.PendingAction
import com.orbit.app.core.domain.model.RiskLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    action: PendingAction,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    val badgeColor = when (action.riskLevel) {
        RiskLevel.Low -> OrbitTheme.colors.glowPrimary
        RiskLevel.Medium -> OrbitTheme.colors.warning
        RiskLevel.High -> OrbitTheme.colors.danger
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = action.toolName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = action.riskLevel.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = action.describeActionText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Deny")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
