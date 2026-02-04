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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.meshtastic.core.datastore.UiPreferencesDataSource
import org.meshtastic.feature.emergency.data.BasicSurvivalTopic
import org.meshtastic.feature.emergency.data.DisasterSurvivalTopic
import org.meshtastic.feature.emergency.data.EmergencyGuideData
import org.meshtastic.feature.emergency.data.EmergencyRepository
import org.meshtastic.feature.emergency.data.FirstAidTopic
import javax.inject.Inject

sealed interface EmergencyUiState {
    data object Loading : EmergencyUiState
    data class Success(val data: EmergencyGuideData) : EmergencyUiState
    data class Error(val message: String) : EmergencyUiState
}

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val emergencyRepository: EmergencyRepository,
    private val uiPreferencesDataSource: UiPreferencesDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmergencyUiState>(EmergencyUiState.Loading)
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    val disclaimerAccepted: StateFlow<Boolean> = uiPreferencesDataSource.emergencyDisclaimerAccepted

    init {
        viewModelScope.launch {
            emergencyRepository.getGuideData()
                .onSuccess { _uiState.value = EmergencyUiState.Success(it) }
                .onFailure { _uiState.value = EmergencyUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun acceptDisclaimer() {
        uiPreferencesDataSource.setEmergencyDisclaimerAccepted(true)
    }

    fun getFirstAidTopic(topicId: String): FirstAidTopic? =
        emergencyRepository.getFirstAidTopic(topicId)

    fun getDisasterSurvivalTopic(topicId: String): DisasterSurvivalTopic? =
        emergencyRepository.getDisasterSurvivalTopic(topicId)

    fun getBasicSurvivalTopic(topicId: String): BasicSurvivalTopic? =
        emergencyRepository.getBasicSurvivalTopic(topicId)
}

