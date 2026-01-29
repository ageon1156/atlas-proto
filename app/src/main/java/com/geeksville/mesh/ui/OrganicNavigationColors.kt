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

package com.geeksville.mesh.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Organic color scheme for navigation items
 *
 * Uses nature-inspired colors with smooth transitions
 */
@Composable
fun organicNavigationSuiteColors(): NavigationSuiteItemColors {
    val scheme = MaterialTheme.colorScheme

    return NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            // Selected state - Primary organic color
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            indicatorColor = scheme.primaryContainer.copy(alpha = 0.5f),

            // Unselected state - Muted organic colors
            unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.7f),
            unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.7f),

            // Disabled state
            disabledIconColor = scheme.onSurface.copy(alpha = 0.38f),
            disabledTextColor = scheme.onSurface.copy(alpha = 0.38f),
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            // Selected state for navigation rail
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            indicatorColor = scheme.primaryContainer.copy(alpha = 0.5f),

            // Unselected state for navigation rail
            unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.7f),
            unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.7f),

            // Disabled state
            disabledIconColor = scheme.onSurface.copy(alpha = 0.38f),
            disabledTextColor = scheme.onSurface.copy(alpha = 0.38f),
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            // Selected state for navigation drawer
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            selectedContainerColor = scheme.primaryContainer.copy(alpha = 0.3f),

            // Unselected state
            unselectedIconColor = scheme.onSurfaceVariant,
            unselectedTextColor = scheme.onSurfaceVariant,
            unselectedContainerColor = Color.Transparent,
        )
    )
}
