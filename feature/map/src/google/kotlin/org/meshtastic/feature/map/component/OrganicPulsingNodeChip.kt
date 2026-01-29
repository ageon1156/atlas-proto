/*
 * Copyright (c) 2025-2026 Meshtastic LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.meshtastic.feature.map.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.meshtastic.core.database.model.Node
import org.meshtastic.core.ui.component.NodeChip
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Organic styled pulsing node chip with nature-inspired animation.
 * Features a softer, more flowing pulse effect compared to the standard version.
 * The pulse uses organic easing and the primary color from the theme.
 */
@Composable
fun OrganicPulsingNodeChip(node: Node, modifier: Modifier = Modifier) {
    val animatedProgress = remember { Animatable(0f) }
    val pulseColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(node) {
        // Trigger pulse for nodes heard in the last 5 seconds
        if ((System.currentTimeMillis().milliseconds.inWholeSeconds - node.lastHeard.seconds.inWholeSeconds) <= 5) {
            launch {
                animatedProgress.snapTo(0f)
                // Organic animation with slower, more natural easing
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1200, // Slightly slower for organic feel
                        easing = FastOutSlowInEasing
                    ),
                )
            }
        }
    }

    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            if (animatedProgress.value > 0 && animatedProgress.value < 1f) {
                // Organic pulse with softer alpha curve
                val progress = animatedProgress.value
                // Use ease-out curve for more natural fade
                val alpha = (1f - progress * progress) * 0.25f
                // Slight scale effect for the glow
                val scale = 1f + (progress * 0.1f)

                drawRoundRect(
                    size = size.copy(
                        width = size.width * scale,
                        height = size.height * scale
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = -size.width * (scale - 1f) / 2,
                        y = -size.height * (scale - 1f) / 2
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    color = pulseColor.copy(alpha = alpha),
                )
            }
        },
    ) {
        NodeChip(node = node)
    }
}
