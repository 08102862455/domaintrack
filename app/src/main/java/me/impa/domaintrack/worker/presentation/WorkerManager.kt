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

import android.content.Context
import android.icu.util.Calendar
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import me.impa.domaintrack.core.di.DefaultDispatcher
import me.impa.domaintrack.core.domain.usecase.GetSettings
import me.impa.domaintrack.core.presentation.state.StartStopTimes
import me.impa.domaintrack.core.presentation.state.toStartStopTimes
import me.impa.domaintrack.core.util.msToDateTimeString
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Singleton
class WorkerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsFlow: GetSettings
) {

    private val workManager by lazy { WorkManager.getInstance(context) }

    val workerRunTimeFlow: Flow<Long>
        get() = workManager.getWorkInfosForUniqueWorkFlow(WORKER_NAME)
            .map { infos ->
                infos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }?.nextScheduleTimeMillis ?: 0L
            }

    private suspend fun isAlreadyQueued(todayWindow: StartStopTimes): Boolean {
        val enqueuedRunTime = workerRunTimeFlow.firstOrNull()?.takeIf { it > 0 }?.let { runTime ->
            Calendar.getInstance().apply {
                timeInMillis = runTime
            }
        } ?: return false

        val nextWindowStart = (todayWindow.start.clone() as Calendar).apply { add(Calendar.DATE, 1) }
        val nextWindowStop = (todayWindow.stop.clone() as Calendar).apply { add(Calendar.DATE, 1) }
        val currentDate = Calendar.getInstance()

        return when {
            currentDate.before(todayWindow.start) ->
                enqueuedRunTime.after(todayWindow.start) && enqueuedRunTime.before(todayWindow.stop)

            enqueuedRunTime.after(todayWindow.start) && enqueuedRunTime.before(todayWindow.stop) -> true

            else -> enqueuedRunTime.after(nextWindowStart) && enqueuedRunTime.before(nextWindowStop)
        }

    }

    suspend fun enqueueNextRun(policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {

        val settings = getSettingsFlow().first()

        if (!settings.monitoring.enableMonitoring) {
            Timber.d("Monitoring disabled")
            return
        }

        val todayWindow = settings.monitoring.toStartStopTimes()

        val isQueued = isAlreadyQueued(settings.monitoring.toStartStopTimes())
        if (isQueued) {
            Timber.d("Worker already queued")
            return
        }

        val currentDate = Calendar.getInstance()

        val startDate = todayWindow.start

        if (startDate.before(currentDate)) {
            startDate.add(Calendar.DATE, 1)
        }

        Timber.d("Next worker run at ${startDate.time}")

        val initialDelay = startDate.timeInMillis - currentDate.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequest.Builder(RefreshDomainWorker::class.java)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            WORKER_NAME,
            policy,
            request
        )
    }

    suspend fun appendNextRun() {
        enqueueNextRun(ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    fun cancelWorker() {
        Timber.d("Cancel worker")
        workManager.cancelUniqueWork(WORKER_NAME)
    }

    companion object {
        private const val WORKER_NAME = "RefreshDomainWorker"
    }
}