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

package me.impa.domaintrack.core.domain.model

data class Settings(
    val theme: AppThemeMode,
    val amoled: Boolean,
    val monitoring: MonitoringSettings,
    val domainAlert: AlertSettings,
    val certAlert: AlertSettings,
    val notificationEnabled: Boolean
) {

    companion object {
        val DEFAULT = Settings(
            theme = AppThemeMode.AUTO,
            amoled = false,
            monitoring = MonitoringSettings(
                enableMonitoring = false,
                scheduleTimeStart = 10 * 60,
                scheduleTimeEnd = 20 * 60

            ),
            domainAlert = AlertSettings(
                redLevel = 7,
                yellowLevel = 20
            ),
            certAlert = AlertSettings(
                redLevel = 7,
                yellowLevel = 20
            ),
            notificationEnabled = false
        )

    }
}

data class AlertSettings(
    val redLevel: Int,
    val yellowLevel: Int,
)

data class MonitoringSettings(
    val enableMonitoring: Boolean,
    val scheduleTimeStart: Int,
    val scheduleTimeEnd: Int,
)

enum class AppThemeMode {
    AUTO,
    LIGHT,
    DARK
}
