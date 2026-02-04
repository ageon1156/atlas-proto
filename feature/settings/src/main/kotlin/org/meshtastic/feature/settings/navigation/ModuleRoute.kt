/*
 * Copyright (c) 2025 Meshtastic LLC
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

package org.meshtastic.feature.settings.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Usb
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.meshtastic.core.navigation.Route
import org.meshtastic.core.navigation.SettingsRoutes
import org.meshtastic.core.strings.Res
import org.meshtastic.core.strings.canned_message
import org.meshtastic.core.strings.external_notification
import org.meshtastic.core.strings.serial
import org.meshtastic.core.strings.store_forward
import org.meshtastic.proto.AdminProtos
import org.meshtastic.proto.MeshProtos.DeviceMetadata

/**
 * Module configuration routes for Meshtastic radio modules.
 *
 * Removed for emergency app streamlining:
 * - MQTT, Range Test, Telemetry, Audio, Remote Hardware
 * - Neighbor Info, Ambient Lighting, Detection Sensor, Paxcounter
 *
 * Kept for core functionality:
 * - SERIAL, EXT_NOTIFICATION, STORE_FORWARD, CANNED_MESSAGE
 */
enum class ModuleRoute(val title: StringResource, val route: Route, val icon: ImageVector?, val type: Int = 0) {
    SERIAL(
        Res.string.serial,
        SettingsRoutes.Serial,
        Icons.Default.Usb,
        AdminProtos.AdminMessage.ModuleConfigType.SERIAL_CONFIG_VALUE,
    ),
    EXT_NOTIFICATION(
        Res.string.external_notification,
        SettingsRoutes.ExtNotification,
        Icons.Default.Notifications,
        AdminProtos.AdminMessage.ModuleConfigType.EXTNOTIF_CONFIG_VALUE,
    ),
    STORE_FORWARD(
        Res.string.store_forward,
        SettingsRoutes.StoreForward,
        Icons.AutoMirrored.Default.Forward,
        AdminProtos.AdminMessage.ModuleConfigType.STOREFORWARD_CONFIG_VALUE,
    ),
    CANNED_MESSAGE(
        Res.string.canned_message,
        SettingsRoutes.CannedMessage,
        Icons.AutoMirrored.Default.Message,
        AdminProtos.AdminMessage.ModuleConfigType.CANNEDMSG_CONFIG_VALUE,
    ),
    ;

    val bitfield: Int
        get() = 1 shl ordinal

    companion object {
        fun filterExcludedFrom(metadata: DeviceMetadata?): List<ModuleRoute> = entries.filter {
            when (metadata) {
                null -> true // Include all routes if metadata is null
                else -> metadata.excludedModules and it.bitfield == 0
            }
        }
    }
}

