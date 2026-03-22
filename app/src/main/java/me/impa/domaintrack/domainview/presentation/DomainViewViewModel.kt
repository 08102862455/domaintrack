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

package me.impa.domaintrack.domainview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.domain.usecase.GetDomains
import me.impa.domaintrack.core.domain.usecase.GetSettings
import me.impa.domaintrack.core.presentation.AppMessageBus
import me.impa.domaintrack.core.presentation.NetworkObserver
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.core.presentation.state.AppMessage
import me.impa.domaintrack.core.presentation.state.toState
import me.impa.domaintrack.domainview.domain.usecase.RefreshDomainAndCerts
import me.impa.domaintrack.domainview.presentation.state.DomainViewAction
import me.impa.domaintrack.domainview.presentation.state.DomainViewState

@Suppress("LongParameterList")
@HiltViewModel(assistedFactory = DomainViewViewModel.DomainViewViewModelFactory::class)
class DomainViewViewModel @AssistedInject constructor(
    @Assisted private val domainId: Long,
    getDomains: GetDomains,
    getSettings: GetSettings,
    private val navigator: Navigator,
    private val refreshDomainAndCerts: RefreshDomainAndCerts,
    private val networkObserver: NetworkObserver,
    private val messageBus: AppMessageBus
) : ViewModel() {

    val state: StateFlow<DomainViewState>
        field = MutableStateFlow(DomainViewState())

    fun onAction(intent: DomainViewAction) {
        when (intent) {
            DomainViewAction.Delete -> state.value.domain?.let {
                navigator.navigate(Route.DeleteDomain(it.id))
            }

            DomainViewAction.Edit -> state.value.domain?.let {
                navigator.navigate(Route.EditDomain(domainId, it.domain))
            }

            DomainViewAction.GoBack -> navigator.goBack()

            DomainViewAction.Refresh -> viewModelScope.launch {
                if (!state.value.isConnected) {
                    messageBus.sendMessage(AppMessage.NoConnection)
                } else {
                    refreshDomainAndCerts(domainId)
                }
            }
        }
    }

    init {
        combine(
            getSettings().map { it.domainAlert to it.certAlert }.distinctUntilChanged(),
            getDomains(domainId).filterNotNull()
        ) { (domainAlert, certAlert), domain ->
            domain.toState(domainAlert, certAlert)
        }.onEach { domain ->
            state.update { state ->
                state.copy(
                    domain = domain, isLoading = false,
                    isRefreshing = domain.isUpdating || domain.certificates.any { it.isUpdating })
            }
        }.launchIn(viewModelScope)
        networkObserver.isConnected.distinctUntilChanged().onEach { isConnected ->
            state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)
    }

    @AssistedFactory
    interface DomainViewViewModelFactory {
        fun create(domainId: Long): DomainViewViewModel
    }

}