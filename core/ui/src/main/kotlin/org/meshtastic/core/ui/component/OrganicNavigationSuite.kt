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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Organic Navigation Styling
 *
 * Provides organic shapes for navigation components to create
 * a more natural, flowing appearance while maintaining functionality.
 */

/**
 * Organic shape for bottom navigation bar
 *
 * Rounded top corners for natural, flowing appearance
 */
val OrganicBottomBarShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/**
 * Organic shape for navigation rail
 *
 * Rounded corners on the right side (for LTR) or left (for RTL)
 */
val OrganicNavigationRailShape = RoundedCornerShape(
    topEnd = 28.dp,
    bottomEnd = 28.dp
)

