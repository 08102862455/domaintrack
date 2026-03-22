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

package me.impa.domaintrack.settings.presentation.state

import me.impa.domaintrack.core.domain.model.AppThemeMode

sealed interface SettingsAction {
    data class SetTheme(val theme: AppThemeMode) : SettingsAction
    data class SetAmoled(val amoled: Boolean) : SettingsAction
    data class SetMonitoring(val enabled: Boolean) : SettingsAction
    data object EditSchedule : SettingsAction
    data class SetDomainRedAlert(val days: Int) : SettingsAction
    data class SetDomainYellowAlert(val days: Int) : SettingsAction
    data class SetCertRedAlert(val days: Int) : SettingsAction
    data class SetCertYellowAlert(val days: Int) : SettingsAction
    data class SetNotificationEnabled(val enabled: Boolean) : SettingsAction
    data object NavigateToAlertRules : SettingsAction
    data object NavigateToBatteryOptimization : SettingsAction
    data object GoBack : SettingsAction
}

