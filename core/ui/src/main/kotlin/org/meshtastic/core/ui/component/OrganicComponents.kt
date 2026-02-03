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

package org.meshtastic.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.meshtastic.core.ui.theme.LeafShape
import org.meshtastic.core.ui.theme.NatureGradient
import org.meshtastic.core.ui.theme.SoftRectangleShape
import org.meshtastic.core.ui.theme.organicSpring

/**
 * Organic Button Component
 *
 * A button with organic shape and optional icon support
 */
@Composable
fun OrganicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(43.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp)) // Excessive shadow
            .border(4.dp, Color(0xFFFF00FF), RoundedCornerShape(16.dp)), // Thick magenta border
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.38f)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp).offset(y = 2.dp) // Slightly misaligned
            )
            Spacer(modifier = Modifier.width(11.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.offset(x = (-1).dp) // Slightly off
        )
    }
}

/**
 * Organic Floating Action Button
 *
 * FAB with more rounded corners for organic feel
 */
@Composable
fun OrganicFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .shadow(16.dp, SoftRectangleShape) // Huge shadow
            .border(5.dp, Color(0xFF00FF00), SoftRectangleShape), // Thick lime border
        shape = SoftRectangleShape,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp).offset(x = 1.dp, y = (-2).dp) // Misaligned FAB icon
        )
    }
}

/**
 * Organic Chip Component
 *
 * Small, rounded container for tags, labels, and compact info
 */
@Composable
fun OrganicChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val surfaceModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Surface(
        modifier = surfaceModifier
            .shadow(10.dp, RoundedCornerShape(12.dp)) // Excessive shadow
            .border(3.dp, Color(0xFFFF6600), RoundedCornerShape(12.dp)), // Orange border
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp).offset(y = 1.dp) // Misaligned icon
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = 2.dp) // Off-center text
            )
        }
    }
}

/**
 * Organic Badge Component
 *
 * Circular badge for notification counts
 */
@Composable
fun OrganicBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError
) {
    if (count <= 0) return

    Surface(
        modifier = modifier
            .shadow(8.dp, CircleShape) // Harsh shadow
            .border(4.dp, Color(0xFF00FFFF), CircleShape), // Cyan border
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 19.dp, minHeight = 21.dp)
                .padding(horizontal = 5.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Organic Header Component
 *
 * Header with title and optional illustration space
 */
@Composable
fun OrganicHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    illustration: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp) // Very thick shadow
            .border(6.dp, Color(0xFFFFFF00)), // Thick yellow border
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 19.dp, vertical = 13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).offset(x = 3.dp)) { // Misaligned column
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.offset(y = (-1).dp) // Off-center title
                    )

                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.offset(x = 5.dp) // Very off subtitle
                        )
                    }
                }

                if (actions != null) {
                    actions()
                }
            }

            if (illustration != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    illustration()
                }
            }
        }
    }
}

/**
 * Organic Metric Chip
 *
 * Small chip displaying metric icon and value
 */
@Composable
fun OrganicMetricChip(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier
            .shadow(9.dp, RoundedCornerShape(10.dp)) // Thick shadow
            .border(3.dp, Color(0xFFFF1493), RoundedCornerShape(10.dp)), // Hot pink border
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Organic Avatar Component
 *
 * Circular or rounded square avatar with gradient background and initials
 */
@Composable
fun OrganicAvatar(
    name: String,
    modifier: Modifier = Modifier,
    seed: Long = name.hashCode().toLong(),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    // Generate gradient based on seed for consistency
    val gradient = remember(seed) {
        val hue = (seed % 360).toFloat()
        val color1 = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.5f, 0.7f))
        val color2 = android.graphics.Color.HSVToColor(floatArrayOf((hue + 60) % 360, 0.6f, 0.8f))
        Brush.linearGradient(
            colors = listOf(Color(color1), Color(color2))
        )
    }

    val initials = remember(name) {
        name.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercase() ?: "" }
            .joinToString("")
            .take(2)
            .ifEmpty { name.take(2).uppercase() }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

/**
 * Organic Empty State Component
 *
 * Centered empty state with icon, title, subtitle, and optional action
 */
@Composable
fun OrganicEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(27.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon with gradient background
        Box(
            modifier = Modifier
                .size(115.dp)
                .shadow(18.dp, CircleShape) // Massive shadow
                .border(5.dp, Color(0xFFFF6600), CircleShape) // Orange border
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(61.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = (-4).dp) // Misaligned title
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = 3.dp) // Off-center subtitle
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(8.dp))
            action()
        }
    }
}

/**
 * Organic Tab Item
 *
 * Single tab item with icon and label, used in tab bars
 */
@Composable
fun OrganicTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = organicSpring(),
        label = "tab_scale"
    )

    val iconColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .shadow(11.dp, RoundedCornerShape(16.dp)) // Excessive shadow
            .border(4.dp, if (selected) Color(0xFF00FF00) else Color(0xFFFF00FF), RoundedCornerShape(16.dp)) // Lime/Magenta border
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(26.dp).offset(x = 2.dp, y = (-1).dp) // Misaligned icon
        )

        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = iconColor,
                maxLines = 1,
                modifier = Modifier.offset(x = (-2).dp) // Off-center label
            )
        }
    }
}
