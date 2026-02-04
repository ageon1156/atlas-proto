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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.meshtastic.core.strings.Res
import org.meshtastic.core.strings.manage_map_layers
import org.meshtastic.core.strings.map_filter
import org.meshtastic.core.strings.map_tile_source
import org.meshtastic.core.strings.orient_north
import org.meshtastic.core.strings.toggle_my_position
import org.meshtastic.core.ui.theme.StatusColors.StatusRed
import org.meshtastic.core.ui.theme.organicSpring
import org.meshtastic.feature.map.MapViewModel

/**
 * Organic styled map controls overlay with flowing design and natural animations.
 * Features a river-shaped container with soft shadows and organic button spacing.
 */
@Composable
fun OrganicMapControlsOverlay(
    modifier: Modifier = Modifier,
    mapFilterMenuExpanded: Boolean,
    onMapFilterMenuDismissRequest: () -> Unit,
    onToggleMapFilterMenu: () -> Unit,
    mapViewModel: MapViewModel,
    mapTypeMenuExpanded: Boolean,
    onMapTypeMenuDismissRequest: () -> Unit,
    onToggleMapTypeMenu: () -> Unit,
    onManageLayersClicked: () -> Unit,
    onManageCustomTileProvidersClicked: () -> Unit,
    isNodeMap: Boolean,
    hasLocationPermission: Boolean = false,
    isLocationTrackingEnabled: Boolean = false,
    onToggleLocationTracking: () -> Unit = {},
    bearing: Float = 0f,
    onCompassClick: () -> Unit = {},
    followPhoneBearing: Boolean,
) {
    // Organic container with flowing shape
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compass button with organic styling
            OrganicCompassButton(
                onClick = onCompassClick,
                bearing = bearing,
                isFollowing = followPhoneBearing
            )

            if (isNodeMap) {
                OrganicMapButton(
                    icon = Icons.Outlined.Tune,
                    contentDescription = stringResource(Res.string.map_filter),
                    onClick = onToggleMapFilterMenu,
                )
                NodeMapFilterDropdown(
                    expanded = mapFilterMenuExpanded,
                    onDismissRequest = onMapFilterMenuDismissRequest,
                    mapViewModel = mapViewModel,
                )
            } else {
                Box {
                    OrganicMapButton(
                        icon = Icons.Outlined.Tune,
                        contentDescription = stringResource(Res.string.map_filter),
                        onClick = onToggleMapFilterMenu,
                    )
                    MapFilterDropdown(
                        expanded = mapFilterMenuExpanded,
                        onDismissRequest = onMapFilterMenuDismissRequest,
                        mapViewModel = mapViewModel,
                    )
                }
            }

            Box {
                OrganicMapButton(
                    icon = Icons.Outlined.Map,
                    contentDescription = stringResource(Res.string.map_tile_source),
                    onClick = onToggleMapTypeMenu,
                )
                MapTypeDropdown(
                    expanded = mapTypeMenuExpanded,
                    onDismissRequest = onMapTypeMenuDismissRequest,
                    mapViewModel = mapViewModel,
                    onManageCustomTileProvidersClicked = onManageCustomTileProvidersClicked,
                )
            }

            OrganicMapButton(
                icon = Icons.Outlined.Layers,
                contentDescription = stringResource(Res.string.manage_map_layers),
                onClick = onManageLayersClicked,
            )

            // Location tracking button with organic styling
            if (hasLocationPermission) {
                OrganicMapButton(
                    icon = if (isLocationTrackingEnabled) {
                        Icons.Default.LocationDisabled
                    } else {
                        Icons.Outlined.MyLocation
                    },
                    iconTint = if (isLocationTrackingEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        null
                    },
                    contentDescription = stringResource(Res.string.toggle_my_position),
                    onClick = onToggleLocationTracking,
                )
            }
        }
    }
}

/**
 * Organic styled compass button with rotation animation.
 */
@Composable
private fun OrganicCompassButton(
    onClick: () -> Unit,
    bearing: Float,
    isFollowing: Boolean
) {
    val icon = if (isFollowing) Icons.Filled.Navigation else Icons.Outlined.Navigation

    // Smooth rotation animation
    val animatedBearing by animateFloatAsState(
        targetValue = -bearing,
        animationSpec = organicSpring(),
        label = "compass_rotation"
    )

    OrganicMapButton(
        modifier = Modifier.rotate(animatedBearing),
        icon = icon,
        iconTint = if (bearing == 0f) MaterialTheme.colorScheme.StatusRed else null,
        contentDescription = stringResource(Res.string.orient_north),
        onClick = onClick,
    )
}

