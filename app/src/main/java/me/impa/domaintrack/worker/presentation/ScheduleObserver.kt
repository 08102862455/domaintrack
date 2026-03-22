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

package me.impa.domaintrack.worker.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingWorkPolicy
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.impa.domaintrack.core.di.DefaultDispatcher
import me.impa.domaintrack.core.domain.usecase.GetSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleObserver @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val getSettingsFlow: GetSettings,
    private val workerManager: WorkerManager
) : DefaultLifecycleObserver {

    private val scopes = mutableMapOf<LifecycleOwner, CoroutineScope>()

    @OptIn(FlowPreview::class)
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        scopes.remove(owner)?.cancel()
        scopes[owner] = CoroutineScope(defaultDispatcher + SupervisorJob()).also { localScope ->
            getSettingsFlow().map { it.monitoring }.distinctUntilChanged()
                .debounce(500).onEach {
                    if (it.enableMonitoring)
                        workerManager.enqueueNextRun(ExistingWorkPolicy.REPLACE)
                    else
                        workerManager.cancelWorker()
                }.launchIn(localScope)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        scopes.remove(owner)?.cancel()
    }
}