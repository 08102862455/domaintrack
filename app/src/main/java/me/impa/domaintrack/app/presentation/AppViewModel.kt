/*
 * Copyright (c) 2026 Alexander Yaburov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.impa.domaintrack.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.impa.domaintrack.alert.presentation.AlertManager
import me.impa.domaintrack.app.presentation.state.AppState
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.domain.InternalDataStore
import me.impa.domaintrack.core.domain.usecase.RefreshAllDomains
import me.impa.domaintrack.core.presentation.AppMessageBus
import me.impa.domaintrack.core.presentation.navigation.Navigator
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class AppViewModel @Inject constructor(
    repository: DomainTrackRepository,
    alertManager: AlertManager,
    appMessageBus: AppMessageBus,
    val navigator: Navigator,
    private val internalDataStore: InternalDataStore,
    private val refreshAllDomains: RefreshAllDomains,
) : ViewModel() {

    val state: StateFlow<AppState>
        field = MutableStateFlow(AppState())

    val messageBus = appMessageBus.messages

    init {
        repository.settingsManager.settingsFlow.onEach { settings ->
            state.update { it.copy(settings = settings) }
        }.launchIn(viewModelScope)
        alertManager.alerts.map { it.size }.onEach { alertCount ->
            state.update {
                it.copy(
                    alertCount = alertCount
                )
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            checkAndUpdateDomains()
        }
    }

    private suspend fun checkAndUpdateDomains() {
        val lastUpdateTime = internalDataStore.getLastFullUpdateTime().first()
        val timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime

        if (timeSinceLastUpdate > 12.hours.inWholeMilliseconds) {
            refreshAllDomains()
        }
    }

}