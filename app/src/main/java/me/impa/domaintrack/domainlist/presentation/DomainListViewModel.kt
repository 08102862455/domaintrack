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

package me.impa.domaintrack.domainlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.domain.usecase.GetDomains
import me.impa.domaintrack.core.domain.usecase.GetInternalDataStore
import me.impa.domaintrack.core.domain.usecase.GetSettings
import me.impa.domaintrack.core.domain.usecase.RefreshAllDomains
import me.impa.domaintrack.core.presentation.AppMessageBus
import me.impa.domaintrack.core.presentation.NetworkObserver
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.core.presentation.state.AppMessage
import me.impa.domaintrack.domainlist.presentation.state.DomainListState
import me.impa.domaintrack.core.presentation.state.toState
import me.impa.domaintrack.core.util.msToDateTimeString
import me.impa.domaintrack.domainlist.presentation.state.DomainListAction
import me.impa.domaintrack.domainlist.presentation.state.SortType
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class DomainListViewModel @Inject constructor(
    getDomains: GetDomains,
    getSettings: GetSettings,
    getInternalDataStore: GetInternalDataStore,
    private val refreshAllDomains: RefreshAllDomains,
    val navigator: Navigator,
    private val networkObserver: NetworkObserver,
    private val messageBus: AppMessageBus
) : ViewModel() {

    val state: StateFlow<DomainListState>
        field = MutableStateFlow(DomainListState())

    private val internalDataStore = getInternalDataStore()

    fun onAction(action: DomainListAction) {
        when (action) {
            is DomainListAction.SelectDomain -> navigator.navigate(Route.DomainDetails(action.domainId))
            DomainListAction.AddDomain -> navigator.navigate(Route.AddDomain)
            DomainListAction.Refresh -> viewModelScope.launch {
                if (!state.value.isConnected) {
                    messageBus.sendMessage(AppMessage.NoConnection)
                } else {
                    refreshAllDomains()
                }
            }

            is DomainListAction.SetSortType ->
                viewModelScope.launch { internalDataStore.setListSortType(action.sortType.ordinal) }
        }
    }

    init {
        combine(
            getSettings().map { it.domainAlert to it.certAlert }.distinctUntilChanged(),
            getDomains()
        ) { (domainAlert, certAlert), domains ->
            domains.map { it.toState(domainAlert, certAlert) }
        }.onEach { domains ->
            state.update { uiState ->
                uiState.copy(
                    domains = domains,
                    isLoading = false,
                    isRefreshing = domains.any { it.isUpdating }
                            || domains.flatMap { it.certificates }.any { it.isUpdating }
                )
            }
        }.launchIn(viewModelScope)
        networkObserver.isConnected.distinctUntilChanged().onEach { isConnected ->
            state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)
        combine(
            internalDataStore.getLastFullUpdateTime(),
            internalDataStore.getListSortType()
        ) { lastUpdated, sortType ->
            lastUpdated to sortType
        }.onEach { (lastUpdated, sortType) ->
            state.update { state ->
                state.copy(
                    sortType = SortType.entries.toTypedArray().getOrElse(sortType) { SortType.NAME },
                    lastUpdated = lastUpdated.takeIf { it > 0 }?.let { msToDateTimeString(it) }
                )
            }
        }.launchIn(viewModelScope)
    }
}