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

package me.impa.domaintrack.alert.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import me.impa.domaintrack.alert.domain.model.AlertEntity
import me.impa.domaintrack.alert.presentation.state.AlertsAction
import me.impa.domaintrack.alert.presentation.state.AlertsState
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.core.presentation.state.AlertLevel
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    alertManager: AlertManager,
    private val navigator: Navigator
) : ViewModel() {

    val state: StateFlow<AlertsState>
        field = MutableStateFlow(AlertsState())

    fun onAction(action: AlertsAction) {
        when (action) {
            is AlertsAction.OpenDomain -> navigator.navigate(Route.DomainDetails(action.id))
            AlertsAction.GoBack -> navigator.goBack()
        }
    }

    init {

        alertManager.alerts
            .onEach { alerts ->
                state.update { alertsState ->
                    alertsState.copy(
                        redAlerts = alerts.filter { it.level == AlertLevel.RED }
                            .sortedWith(compareBy<AlertEntity> { it.daysLeft }.thenBy { it.subject }),
                        yellowAlerts = alerts.filter { it.level == AlertLevel.YELLOW }
                            .sortedWith(compareBy<AlertEntity> { it.daysLeft }.thenBy { it.subject })
                    )
                }
            }.launchIn(viewModelScope)

    }

}