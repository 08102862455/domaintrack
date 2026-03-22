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

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.impa.domaintrack.R
import me.impa.domaintrack.alert.domain.model.AlertEntity
import me.impa.domaintrack.alert.presentation.AlertManager
import me.impa.domaintrack.core.di.DefaultDispatcher
import me.impa.domaintrack.core.domain.InternalDataStore
import me.impa.domaintrack.core.domain.usecase.GetDomains
import me.impa.domaintrack.core.domain.usecase.GetSettings
import me.impa.domaintrack.core.domain.usecase.RefreshCert
import me.impa.domaintrack.core.domain.usecase.RefreshDomain
import me.impa.domaintrack.core.presentation.state.AlertLevel
import me.impa.domaintrack.core.presentation.state.toStartStopTimes
import me.impa.domaintrack.ui.MainActivity
import timber.log.Timber

@Suppress("LongParameterList")
@HiltWorker
class RefreshDomainWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workerManager: WorkerManager,
    private val alertManager: AlertManager,
    private val internalDataStore: InternalDataStore,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val getDomains: GetDomains,
    private val getSettings: GetSettings,
    private val refreshDomain: RefreshDomain,
    private val refreshCert: RefreshCert
) : CoroutineWorker(context, params) {

    val scope = CoroutineScope(defaultDispatcher + SupervisorJob())
    val notificationManager by lazy { applicationContext.getSystemService<NotificationManager>() }

    @Suppress("ReturnCount")
    override suspend fun doWork(): Result {
        try {
            val settings = getSettings().first()
            val runWindow = settings.monitoring.toStartStopTimes()
            val currentDate = System.currentTimeMillis()
            if (currentDate !in runWindow.start.timeInMillis..runWindow.stop.timeInMillis) {
                Timber.i("Out of run window")
                return Result.success()
            }

            val domains = getDomains().first()

            val domainsUpdateJob = scope.launch {
                domains.forEach {
                    Timber.i("Updating domain ${it.domain.id}")
                    refreshDomain(it.domain.id)
                }
            }
            val certUpdateJob = scope.launch {
                domains.flatMap { it.certificates }
                    .forEach {
                        Timber.i("Updating certificate ${it.id}")
                        refreshCert(it.id)
                    }
            }

            listOf(domainsUpdateJob, certUpdateJob).joinAll()

            internalDataStore.setLastFullUpdateTime(System.currentTimeMillis())

            if (settings.notificationEnabled)
                sendAlerts()

            return Result.success()

        } catch (e: Exception) {
            Timber.e(e)
            return Result.failure()
        } finally {
            workerManager.appendNextRun()
            scope.cancel()
        }
    }

    private suspend fun sendAlerts() {
        val alerts = alertManager.alerts.first().takeIf { it.isNotEmpty() } ?: return

        createNotificationChannels()

        val (channel, priority) = getChannelAndPriority(alerts)
        val title = buildTitle(alerts.size)
        val content = buildContent(alerts)
        val style = buildInboxStyle(alerts)
        val pendingIntent = createPendingIntent()

        val builder = NotificationCompat.Builder(applicationContext, channel)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(style)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
            ?.apply {
                putExtra(EXTRA_FROM_NOTIFICATION, true)
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

            }
            ?: Intent(applicationContext, MainActivity::class.java).apply {
                putExtra(EXTRA_FROM_NOTIFICATION, true)
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        return PendingIntent.getActivity(
            applicationContext,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getChannelAndPriority(alerts: List<AlertEntity>): Pair<String, Int> {
        return if (alerts.any { it.level == AlertLevel.RED }) {
            CHANNEL_CRITICAL to NotificationCompat.PRIORITY_HIGH
        } else {
            CHANNEL_REMINDER to NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun buildTitle(alertsCount: Int): String {
        return applicationContext.resources.getQuantityString(
            R.plurals.notification_title,
            alertsCount,
            applicationContext.getString(R.string.app_name),
            alertsCount
        )
    }

    private fun buildContent(alerts: List<AlertEntity>): String {
        val resources = applicationContext.resources
        val redAlerts = alerts.count { it.level == AlertLevel.RED }
        val yellowAlerts = alerts.count { it.level == AlertLevel.YELLOW }

        return buildString {
            if (redAlerts > 0) {
                append(
                    resources.getQuantityString(
                        R.plurals.notification_text_critical_count,
                        redAlerts, redAlerts
                    )
                )
            }
            if (yellowAlerts > 0) {
                if (redAlerts > 0) append(", ")
                append(
                    resources.getQuantityString(
                        R.plurals.notification_text_reminder_count,
                        yellowAlerts, yellowAlerts
                    )
                )
            }
        }
    }

    private fun buildInboxStyle(alerts: List<AlertEntity>): NotificationCompat.InboxStyle {
        val resources = applicationContext.resources
        return NotificationCompat.InboxStyle().also { style ->
            style.setBigContentTitle(
                resources.getQuantityString(
                    R.plurals.notification_content_title,
                    alerts.size, alerts.size
                )
            )
            alerts.sortedByDescending { it.level }.take(NOTIFICATION_LINES).forEach { alert ->
                when {
                    alert.noInfo -> style.addLine(
                        resources.getString(R.string.notification_text_check_error, alert.subject)
                    )

                    alert is AlertEntity.Certificate -> style.addLine(
                        resources.getQuantityString(
                            R.plurals.notification_text_cert_expiration,
                            alert.daysLeft, alert.subject, alert.daysLeft
                        )
                    )

                    alert is AlertEntity.Domain -> style.addLine(
                        resources.getQuantityString(
                            R.plurals.notification_text_domain_expiration,
                            alert.daysLeft, alert.subject, alert.daysLeft
                        )
                    )
                }
            }
            if (alerts.size > NOTIFICATION_LINES) {
                style.setSummaryText(
                    resources.getString(
                        R.string.notification_text_more,
                        alerts.size - NOTIFICATION_LINES
                    )
                )
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val resources = applicationContext.resources
        NotificationChannel(
            CHANNEL_CRITICAL,
            resources.getString(R.string.channel_notification_critical_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(true)
            lightColor = Color.RED
            description = resources.getString(R.string.channel_notification_critical_description)
        }.also { notificationManager?.createNotificationChannel(it) }

        NotificationChannel(
            CHANNEL_REMINDER,
            resources.getString(R.string.channel_notification_reminder_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = resources.getString(R.string.channel_notification_reminder_description)
        }.also { notificationManager?.createNotificationChannel(it) }
    }

    companion object {
        private const val CHANNEL_CRITICAL = "domaintrack_critical"
        private const val CHANNEL_REMINDER = "domaintrack_reminder"
        private const val NOTIFICATION_ID = 1984
        private const val NOTIFICATION_LINES = 3
        private const val PENDING_INTENT_REQUEST_CODE = 1001
        const val EXTRA_FROM_NOTIFICATION = "extra_from_notification"
    }

}