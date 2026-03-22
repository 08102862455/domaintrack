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

package me.impa.domaintrack.domaindelete.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.impa.domaintrack.core.di.IoDispatcher
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.domaindelete.presentation.state.DomainDeleteAction
import me.impa.domaintrack.domaindelete.presentation.state.DomainDeleteState

@HiltViewModel(assistedFactory = DomainDeleteViewModel.DomainDeleteViewModelFactory::class)
class DomainDeleteViewModel @AssistedInject constructor(
    @Assisted private val domainId: Long,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val navigator: Navigator,
    private val repository: DomainTrackRepository
) : ViewModel() {

    val state: StateFlow<DomainDeleteState>
        field = MutableStateFlow(DomainDeleteState(domainId = domainId))

    fun onAction(action: DomainDeleteAction) {
        when (action) {
            DomainDeleteAction.Cancel -> navigator.goBack()
            DomainDeleteAction.Delete -> viewModelScope.launch {
                repository.deleteDomain(domainId).also { navigator.navigate(Route.DomainList) }
            }
        }
    }

    init {
        viewModelScope.launch {
            state.update {
                it.copy(
                    domain = withContext(ioDispatcher) { repository.getDomain(domainId) },
                    isLoading = false
                )
            }
        }
    }

    @AssistedFactory
    interface DomainDeleteViewModelFactory {
        fun create(domainId: Long): DomainDeleteViewModel
    }
}
