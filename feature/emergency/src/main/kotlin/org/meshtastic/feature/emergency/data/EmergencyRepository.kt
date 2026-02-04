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

package org.meshtastic.feature.emergency.data

import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import org.meshtastic.core.di.CoroutineDispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepository @Inject constructor(
    private val jsonDataSource: EmergencyJsonDataSource,
    private val dispatchers: CoroutineDispatchers,
) {
    @Volatile
    private var cachedData: EmergencyGuideData? = null

    suspend fun getGuideData(): Result<EmergencyGuideData> = withContext(dispatchers.io) {
        cachedData?.let { return@withContext Result.success(it) }
        runCatching {
            jsonDataSource.loadEmergencyGuideData().also { cachedData = it }
        }.onFailure { e ->
            Logger.e(e) { "Failed to load emergency guide data" }
        }
    }

    fun getFirstAidTopic(id: String): FirstAidTopic? =
        cachedData?.firstAid?.get(id)

    fun getDisasterSurvivalTopic(id: String): DisasterSurvivalTopic? =
        cachedData?.disasterSurvival?.get(id)

    fun getBasicSurvivalTopic(id: String): BasicSurvivalTopic? =
        cachedData?.basicSurvival?.get(id)
}

