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

package org.meshtastic.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Medical cross / health and safety icon from Material Symbols.
 *
 * @see
 *   [health_and_safety](https://fonts.google.com/icons?selected=Material+Symbols+Rounded:health_and_safety:FILL@0;wght@400;GRAD@0;opsz@24&icon.style=Rounded&icon.query=health&icon.set=Material+Symbols&icon.size=24&icon.color=%23e3e3e3&icon.platform=android)
 */
val MeshtasticIcons.Emergency: ImageVector
    get() {
        if (emergency != null) {
            return emergency!!
        }
        emergency =
            ImageVector.Builder(
                name = "Emergency",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f,
            )
                .apply {
                    path(fill = SolidColor(Color(0xFFE3E3E3))) {
                        // Shield shape with medical cross
                        moveTo(480f, 880f)
                        quadToRelative(-7f, 0f, -13f, -1f)
                        reflectiveQuadToRelative(-12f, -3f)
                        quadToRelative(-135f, -45f, -215f, -166.5f)
                        reflectiveQuadTo(160f, 444f)
                        verticalLineToRelative(-204f)
                        quadToRelative(0f, -26f, 17f, -45.5f)
                        reflectiveQuadToRelative(43f, -27.5f)
                        lineToRelative(240f, -80f)
                        quadToRelative(10f, -4f, 20f, -4f)
                        reflectiveQuadToRelative(20f, 4f)
                        lineToRelative(240f, 80f)
                        quadToRelative(26f, 8f, 43f, 27.5f)
                        reflectiveQuadToRelative(17f, 45.5f)
                        verticalLineToRelative(204f)
                        quadToRelative(0f, 143f, -80f, 264.5f)
                        reflectiveQuadTo(505f, 876f)
                        quadToRelative(-6f, 2f, -12f, 3f)
                        reflectiveQuadToRelative(-13f, 1f)
                        close()
                        // Cross horizontal bar
                        moveTo(420f, 560f)
                        horizontalLineToRelative(120f)
                        verticalLineToRelative(-80f)
                        horizontalLineToRelative(80f)
                        verticalLineToRelative(-120f)
                        horizontalLineToRelative(-80f)
                        verticalLineToRelative(-80f)
                        horizontalLineToRelative(-120f)
                        verticalLineToRelative(80f)
                        horizontalLineToRelative(-80f)
                        verticalLineToRelative(120f)
                        horizontalLineToRelative(80f)
                        close()
                    }
                }
                .build()
        return emergency!!
    }

private var emergency: ImageVector? = null

