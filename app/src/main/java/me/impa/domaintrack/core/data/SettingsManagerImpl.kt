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

package me.impa.domaintrack.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.impa.domaintrack.core.data.store.AlertConfig
import me.impa.domaintrack.core.data.store.appSettingsDataStore
import me.impa.domaintrack.core.data.store.toDomain
import me.impa.domaintrack.core.data.store.toProto
import me.impa.domaintrack.core.domain.SettingsManager
import me.impa.domaintrack.core.domain.model.AppThemeMode
import me.impa.domaintrack.core.domain.model.Settings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsManager {
    override val settingsFlow = context.appSettingsDataStore.data.map { it.toDomain() }

    override suspend fun saveSettings(settings: Settings) {
        context.appSettingsDataStore.updateData { settings.toProto() }
    }

    override suspend fun setTheme(theme: AppThemeMode) {
        context.appSettingsDataStore.updateData { it.copy(theme_mode = theme.toProto()) }
    }

    override suspend fun setAmoled(amoled: Boolean) {
        context.appSettingsDataStore.updateData { it.copy(amoled_mode = amoled) }
    }

    override suspend fun setMonitoring(enabled: Boolean) {
        context.appSettingsDataStore.updateData { it.copy(background_update = enabled) }
    }

    override suspend fun setMonitoringStart(value: Int) {
        context.appSettingsDataStore.updateData { it.copy(schedule_time_start = value) }
    }

    override suspend fun setMonitoringEnd(value: Int) {
        context.appSettingsDataStore.updateData { it.copy(schedule_time_end = value) }
    }

    override suspend fun setMonitoringSchedule(start: Int, end: Int) {
        context.appSettingsDataStore.updateData { it.copy(schedule_time_start = start, schedule_time_end = end) }
    }

    override suspend fun setDomainAlertLevels(red: Int, yellow: Int) {
        context.appSettingsDataStore.updateData {
            it.copy(domain_alert_cfg = AlertConfig(red_level = red, yellow_level = yellow))
        }
    }

    override suspend fun setCertAlertLevels(red: Int, yellow: Int) {
        context.appSettingsDataStore.updateData {
            it.copy(cert_alert_cfg = AlertConfig(red_level = red, yellow_level = yellow))
        }
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        context.appSettingsDataStore.updateData { it.copy(notification_enabled = enabled) }
    }

}