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

package me.impa.domaintrack.domainedit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.domain.usecase.GetInternalDataStore
import me.impa.domaintrack.core.domain.usecase.GetSettings
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.domainedit.domain.usecase.SaveDomain
import me.impa.domaintrack.domainedit.domain.usecase.IsValidDomain
import me.impa.domaintrack.domainedit.presentation.state.DomainEditAction
import me.impa.domaintrack.domainedit.presentation.state.DomainEditState

@Suppress("LongParameterList")
@HiltViewModel(assistedFactory = DomainEditViewModel.DomainEditViewModelFactory::class)
class DomainEditViewModel @AssistedInject constructor(
    @Assisted private val domainId: Long,
    @Assisted private val domain: String,
    private val repository: DomainTrackRepository,
    private val navigator: Navigator,
    private val saveDomain: SaveDomain,
    private val isValidDomain: IsValidDomain,
    private val getInternalDataStore: GetInternalDataStore,
    private val getSettings: GetSettings
) : ViewModel() {

    private val domainList = repository.getDomainsFlow().map { domainInfoList ->
        domainInfoList.map { it.domain.domain }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val state: StateFlow<DomainEditState>
        field = MutableStateFlow(
            DomainEditState(
                domainName = domain,
                isAddDomain = domainId == 0L,
                isDomainNameValid = isValidDomain(domain, domain, domainList.value)
            )
        )

    fun onAction(intent: DomainEditAction) {
        when (intent) {
            DomainEditAction.Cancel -> navigator.goBack()
            DomainEditAction.SaveDomain -> viewModelScope.launch {
                val savedId = saveDomain(domainId, state.value.domainName)
                val shouldShowOnboarding = shouldShowOnboarding()
                navigator.run {
                    goBack()
                    navigate(Route.DomainDetails(savedId))
                    if (shouldShowOnboarding) {
                        navigate(Route.Onboarding)
                    }
                }
            }

            is DomainEditAction.SetDomain -> state.update {
                it.copy(
                    domainName = intent.domain,
                    isDomainNameValid = isValidDomain(intent.domain, domain, domainList.value)
                )
            }
        }
    }

    private suspend fun shouldShowOnboarding(): Boolean {
        val internalDataStore = getInternalDataStore()
        val onboardingShown = internalDataStore.getMonitorOnboardingShown().first()
        if (!onboardingShown) {
            internalDataStore.setMonitorOnboardingShown(true)
            val settings = getSettings().first()
            if (!settings.monitoring.enableMonitoring) {
                return true
            }

        }
        return false
    }

    @AssistedFactory
    interface DomainEditViewModelFactory {
        fun create(domainId: Long, domain: String): DomainEditViewModel

    }
}