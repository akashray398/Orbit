package com.orbit.app.core.designsystem.showcase

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.components.OrbitButton
import com.orbit.app.core.designsystem.components.OrbitButtonSize
import com.orbit.app.core.designsystem.components.OrbitButtonVariant
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.components.OrbitEmptyState
import com.orbit.app.core.designsystem.components.OrbitErrorState
import com.orbit.app.core.designsystem.components.OrbitLoadingIndicator
import com.orbit.app.core.designsystem.components.OrbitLogo
import com.orbit.app.core.designsystem.components.OrbitLogoMode
import com.orbit.app.core.designsystem.components.OrbitShimmerPlaceholder
import com.orbit.app.core.designsystem.components.OrbitTextField
import com.orbit.app.core.designsystem.theme.OrbitTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DesignSystemShowcaseScreen(
    onBackClick: () -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var chipSelected by remember { mutableStateOf(true) }

    OrbitBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Orbit Design System") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section 0: Brand Logo Marks
                ShowcaseSection(title = "Brand & Logo Marks") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OrbitLogo(size = 24.dp, mode = OrbitLogoMode.Mark)
                            OrbitLogo(size = 48.dp, mode = OrbitLogoMode.Mark)
                            OrbitLogo(size = 72.dp, mode = OrbitLogoMode.Mark)
                        }
                        OrbitLogo(size = 48.dp, mode = OrbitLogoMode.Wordmark)
                        OrbitLogo(size = 48.dp, mode = OrbitLogoMode.Mono, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Section 1: Buttons
                ShowcaseSection(title = "Buttons") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrbitButton(onClick = {}, variant = OrbitButtonVariant.Primary) { Text("Primary") }
                        OrbitButton(onClick = {}, variant = OrbitButtonVariant.Secondary) { Text("Secondary") }
                        OrbitButton(onClick = {}, variant = OrbitButtonVariant.Text) { Text("Text") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrbitButton(onClick = {}, size = OrbitButtonSize.Small) { Text("Small") }
                        OrbitButton(onClick = {}, size = OrbitButtonSize.Medium) { Text("Medium") }
                        OrbitButton(onClick = {}, size = OrbitButtonSize.Large) { Text("Large") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OrbitButton(onClick = {}, loading = true) { Text("Loading") }
                        OrbitButton(onClick = {}, enabled = false) { Text("Disabled") }
                        OrbitButton(
                            onClick = {},
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        ) {
                            Text("Icon")
                        }
                    }
                }

                // Section 2: Text Fields
                ShowcaseSection(title = "Text Fields") {
                    OrbitTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        label = "Agent Name",
                        placeholder = "Enter agent name...",
                        showClearButton = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OrbitTextField(
                        value = passwordValue,
                        onValueChange = { passwordValue = it },
                        label = "API Key / Password",
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OrbitTextField(
                        value = "Invalid input",
                        onValueChange = {},
                        label = "Error Field",
                        isError = true,
                        errorMessage = "Required field cannot be blank"
                    )
                }

                // Section 3: Chips
                ShowcaseSection(title = "Chips") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OrbitChip(
                            label = "Filter Chip",
                            selected = chipSelected,
                            onClick = { chipSelected = !chipSelected }
                        )
                        OrbitChip(
                            label = "Suggestion",
                            selected = false,
                            onClick = {}
                        )
                    }
                }

                // Section 4: Glass Cards
                ShowcaseSection(title = "Glass Cards") {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        enableGlow = true
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Frosted Glass Card with Glow", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Translucent glass effect with dynamic gradient border and API-level backdrop blur fallback.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section 5: Loading & Shimmers
                ShowcaseSection(title = "Loading & Shimmers") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OrbitLoadingIndicator()
                        OrbitShimmerPlaceholder(modifier = Modifier.weight(1f), height = 24.dp)
                    }
                }

                // Section 6: States
                ShowcaseSection(title = "Empty & Error States") {
                    OrbitEmptyState(
                        title = "No Tasks Found",
                        message = "Your AI agent has no pending phone automation tasks.",
                        actionLabel = "Create Task",
                        onActionClick = {}
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OrbitErrorState(
                        message = "Failed to connect to agent proxy service.",
                        onRetry = {}
                    )
                }

                // Section 7: Swatches
                ShowcaseSection(title = "Color Tokens") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorSwatch(name = "Primary", color = OrbitTheme.colors.glowPrimary)
                        ColorSwatch(name = "Secondary", color = OrbitTheme.colors.glowSecondary)
                        ColorSwatch(name = "Success", color = OrbitTheme.colors.success)
                        ColorSwatch(name = "Danger", color = OrbitTheme.colors.danger)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(name = "Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShowcaseDarkPreview() {
    OrbitTheme(darkTheme = true) {
        DesignSystemShowcaseScreen()
    }
}

@Preview(name = "Light Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ShowcaseLightPreview() {
    OrbitTheme(darkTheme = false) {
        DesignSystemShowcaseScreen()
    }
}

@Preview(name = "Font Scale 1.5x", fontScale = 1.5f)
@Composable
private fun ShowcaseFontScalePreview() {
    OrbitTheme(darkTheme = true) {
        DesignSystemShowcaseScreen()
    }
}
