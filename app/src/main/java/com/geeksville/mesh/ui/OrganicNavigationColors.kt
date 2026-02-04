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
 * Minimal, clean color scheme for navigation items.
 *
 * Subtle indicator, subdued unselected states, flat appearance.
 */
@Composable
fun organicNavigationSuiteColors(): NavigationSuiteItemColors {
    val scheme = MaterialTheme.colorScheme

    return NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            indicatorColor = scheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.5f),
            unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            indicatorColor = scheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.5f),
            unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.5f),
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            selectedContainerColor = scheme.primary.copy(alpha = 0.12f),
            unselectedIconColor = scheme.onSurfaceVariant.copy(alpha = 0.5f),
            unselectedTextColor = scheme.onSurfaceVariant.copy(alpha = 0.5f),
            unselectedContainerColor = Color.Transparent,
        )
    )
}

