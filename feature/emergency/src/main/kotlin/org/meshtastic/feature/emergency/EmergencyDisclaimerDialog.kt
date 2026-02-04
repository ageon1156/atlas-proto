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

package org.meshtastic.feature.emergency

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.meshtastic.core.strings.Res
import org.meshtastic.core.strings.emergency_disclaimer_accept
import org.meshtastic.core.strings.emergency_disclaimer_title

@Composable
fun EmergencyDisclaimerDialog(
    disclaimerText: String,
    onAccept: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* non-dismissable */ },
        title = {
            Text(
                text = stringResource(Res.string.emergency_disclaimer_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = disclaimerText,
                    fontSize = 15.sp,
                    color = Color(0xFFE0E0E0),
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This information is emergency guidance only and not a substitute for professional medical care.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF9800),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(
                    text = stringResource(Res.string.emergency_disclaimer_accept),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        },
        containerColor = Color(0xFF212121),
        titleContentColor = Color.White,
    )
}

