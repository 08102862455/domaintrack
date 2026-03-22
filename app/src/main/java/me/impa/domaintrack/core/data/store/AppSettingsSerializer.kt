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

package me.impa.domaintrack.core.data.store

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import me.impa.domaintrack.core.domain.model.AppThemeMode
import me.impa.domaintrack.core.domain.model.MonitoringSettings
import me.impa.domaintrack.core.domain.model.Settings
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings
        get() = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings = try {
        AppSettings.ADAPTER.decode(input)
    } catch (e: Exception) {
        throw CorruptionException("Cannot read proto.", e)
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        AppSettings.ADAPTER.encode(output, t)
    }
}

val Context.appSettingsDataStore by dataStore("app_settings.pb", AppSettingsSerializer)

fun Settings.toProto() = AppSettings(
    theme_mode = theme.toProto(),
    amoled_mode = amoled,
    background_update = monitoring.enableMonitoring,
    schedule_time_start = monitoring.scheduleTimeStart,
    schedule_time_end = monitoring.scheduleTimeEnd,
    domain_alert_cfg = domainAlert.toProto(),
    cert_alert_cfg = certAlert.toProto(),
    notification_enabled = notificationEnabled
)

fun AppSettings.toDomain() = Settings(
    theme = theme_mode?.toDomain() ?: Settings.DEFAULT.theme,
    amoled = amoled_mode ?: Settings.DEFAULT.amoled,
    monitoring = MonitoringSettings(
        enableMonitoring = background_update ?: Settings.DEFAULT.monitoring.enableMonitoring,
        scheduleTimeStart = schedule_time_start ?: Settings.DEFAULT.monitoring.scheduleTimeStart,
        scheduleTimeEnd = schedule_time_end ?: Settings.DEFAULT.monitoring.scheduleTimeEnd
    ),
    domainAlert = domain_alert_cfg?.toDomain() ?: Settings.DEFAULT.domainAlert,
    certAlert = cert_alert_cfg?.toDomain() ?: Settings.DEFAULT.certAlert,
    notificationEnabled = notification_enabled ?: Settings.DEFAULT.notificationEnabled
)
