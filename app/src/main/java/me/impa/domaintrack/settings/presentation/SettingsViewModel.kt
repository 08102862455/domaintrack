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

package me.impa.domaintrack.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.domain.InternalDataStore
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.core.util.msToDateTimeString
import me.impa.domaintrack.settings.presentation.state.SettingsAction
import me.impa.domaintrack.settings.presentation.state.SettingsState
import me.impa.domaintrack.worker.presentation.WorkerManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navigator: Navigator,
    repository: DomainTrackRepository,
    private val internalDataStore: InternalDataStore,
    workerManager: WorkerManager
) : ViewModel() {

    private val settingsManager = repository.settingsManager

    val state: StateFlow<SettingsState>
        field = MutableStateFlow(SettingsState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetAmoled -> viewModelScope.launch { settingsManager.setAmoled(action.amoled) }
            is SettingsAction.SetTheme -> viewModelScope.launch { settingsManager.setTheme(action.theme) }
            is SettingsAction.SetMonitoring -> onSetMonitoring(action.enabled)
            SettingsAction.EditSchedule -> navigator.navigate(Route.EditSchedule)
            is SettingsAction.SetCertRedAlert -> viewModelScope.launch {
                settingsManager.setCertAlertLevels(action.days, state.value.settings.certAlert.yellowLevel)
            }

            is SettingsAction.SetCertYellowAlert -> viewModelScope.launch {
                settingsManager.setCertAlertLevels(state.value.settings.certAlert.redLevel, action.days)
            }

            is SettingsAction.SetDomainRedAlert -> viewModelScope.launch {
                settingsManager.setDomainAlertLevels(action.days, state.value.settings.domainAlert.yellowLevel)

            }

            is SettingsAction.SetDomainYellowAlert -> viewModelScope.launch {
                settingsManager.setDomainAlertLevels(state.value.settings.domainAlert.redLevel, action.days)
            }

            is SettingsAction.SetNotificationEnabled -> viewModelScope.launch {
                settingsManager.setNotificationEnabled(action.enabled)
            }

            SettingsAction.NavigateToAlertRules -> navigator.navigate(Route.AlertRules)

            SettingsAction.NavigateToBatteryOptimization -> navigator.navigate(Route.BatteryOptimization)

            SettingsAction.GoBack -> navigator.goBack()
        }
    }

    private fun onSetMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val wasShown = internalDataStore.getBatteryOptimizationShown().first()
                if (!wasShown) {
                    internalDataStore.setBatteryOptimizationShown(true)
                    navigator.navigate(Route.BatteryOptimization)
                }
            }
            
            settingsManager.setMonitoring(enabled)
        }
    }

    init {

        settingsManager.settingsFlow.onEach { settings ->
            state.update {
                it.copy(
                    settings = settings,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)

        workerManager.workerRunTimeFlow.onEach { time ->
            state.update {
                it.copy(nextRunTime = if (time>0L) msToDateTimeString(time) else null)
            }
        }.launchIn(viewModelScope)

    }

}