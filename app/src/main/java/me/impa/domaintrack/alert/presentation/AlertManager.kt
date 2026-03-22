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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import me.impa.domaintrack.alert.domain.model.AlertEntity
import me.impa.domaintrack.alert.domain.usecase.GetCertAlerts
import me.impa.domaintrack.alert.domain.usecase.GetDomainAlerts
import me.impa.domaintrack.core.di.DefaultDispatcher
import me.impa.domaintrack.core.domain.usecase.GetDomains
import me.impa.domaintrack.core.domain.usecase.GetSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Singleton
class AlertManager @Inject constructor(
    private val getDomainAlerts: GetDomainAlerts,
    private val getCertAlerts: GetCertAlerts,
    private val getDomains: GetDomains,
    private val getSettings: GetSettings,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : DefaultLifecycleObserver {

    private val scopes = mutableMapOf<LifecycleOwner, CoroutineScope>()

    val alerts: StateFlow<List<AlertEntity>>
        field = MutableStateFlow(listOf())

    @OptIn(FlowPreview::class)
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        scopes.remove(owner)?.cancel()
        scopes[owner] = CoroutineScope(defaultDispatcher + SupervisorJob()).also { scope ->
            combine(
                getDomains().distinctUntilChanged().debounce(1.seconds),
                getSettings().distinctUntilChanged().debounce(1.seconds),
                flow {
                    while (true) {
                        emit(Unit)
                        delay(5.minutes)
                    }
                }
            ) { System.currentTimeMillis() }
                .onEach { currentTime ->
                    alerts.update { getDomainAlerts(currentTime).first() + getCertAlerts(currentTime).first() }
                }
                .launchIn(scope)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        scopes.remove(owner)?.cancel()
    }

}