package com.orbit.app.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.motion.shimmer
import com.orbit.app.core.designsystem.theme.OrbitTheme

/**
 * OrbitLoadingIndicator - Animated circular progress spinner matching Orbit theme.
 */
@Composable
fun OrbitLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentDescriptionText: String = "Loading"
) {
    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = contentDescriptionText },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.primary,
            trackColor = OrbitTheme.colors.glassFill,
            strokeWidth = 3.dp
        )
    }
}

/**
 * OrbitShimmerPlaceholder - Skeletal shimmer box placeholder for loading states.
 */
@Composable
fun OrbitShimmerPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    shape: RoundedCornerShape = OrbitTheme.shapes.small as RoundedCornerShape
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .shimmer()
    )
}
