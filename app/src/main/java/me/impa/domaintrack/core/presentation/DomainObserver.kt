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

package me.impa.domaintrack.core.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.di.DefaultDispatcher
import me.impa.domaintrack.core.domain.DomainTrackRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainObserver @Inject constructor(
    val repository: DomainTrackRepository,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher
) : DefaultLifecycleObserver {

    private val scopes = mutableMapOf<LifecycleOwner, CoroutineScope>()

    private val domainJobList = mutableMapOf<Long, Job>()

    private val certJobList = mutableMapOf<Long, Job>()

    @OptIn(FlowPreview::class)
    private fun launchDomainActualizer(scope: CoroutineScope) {
        repository.getStaleDomains()
            .map { domains -> domains.filterNot { domainJobList[it.domain.id]?.isActive ?: false } }
            .filterNot { it.isEmpty() }
            .debounce(500L)
            .onEach { domains ->
                Timber.d("Updating ${domains.size} domains")
                domainJobList.putAll(domains.map {
                    it.domain.id to scope.launch { repository.refreshDomain(it.domain.id) }
                })
            }.launchIn(scope)
    }

    @OptIn(FlowPreview::class)
    private fun launchCertActualizer(scope: CoroutineScope) {
        repository.getCertsToActualize()
            .map { certs -> certs.filterNot { certJobList[it.id]?.isActive ?: false } }
            .filterNot { it.isEmpty() }
            .debounce(500L)
            .onEach { certs ->
                Timber.d("Updating ${certs.size} certificates")
                certJobList.putAll(certs.map {
                    it.id to scope.launch { repository.refreshCert(it.id) }
                })
            }.launchIn(scope)
    }

    @OptIn(FlowPreview::class)
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("Starting domain observer")
        scopes.remove(owner)?.cancel()
        domainJobList.clear()
        certJobList.clear()
        scopes[owner] = CoroutineScope(defaultDispatcher + SupervisorJob())
            .also { domainActualizerScope ->
                launchDomainActualizer(domainActualizerScope)
                launchCertActualizer(domainActualizerScope)
            }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.d("Stopping domain observer")
        scopes.remove(owner)?.cancel()
    }

}