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

package me.impa.domaintrack.core.domain

import kotlinx.coroutines.flow.Flow
import me.impa.domaintrack.core.domain.model.AppThemeMode
import me.impa.domaintrack.core.domain.model.Settings

interface SettingsManager {

    val settingsFlow: Flow<Settings>

    suspend fun saveSettings(settings: Settings)

    suspend fun setTheme(theme: AppThemeMode)

    suspend fun setAmoled(amoled: Boolean)

    suspend fun setMonitoring(enabled: Boolean)

    suspend fun setMonitoringStart(value: Int)

    suspend fun setMonitoringEnd(value: Int)

    suspend fun setMonitoringSchedule(start: Int, end: Int)

    suspend fun setDomainAlertLevels(red: Int, yellow: Int)

    suspend fun setCertAlertLevels(red: Int, yellow: Int)

    suspend fun setNotificationEnabled(enabled: Boolean)
}