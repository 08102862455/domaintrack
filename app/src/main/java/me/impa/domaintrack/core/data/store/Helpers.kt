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

import me.impa.domaintrack.core.domain.model.AlertSettings
import me.impa.domaintrack.core.domain.model.AppThemeMode
import me.impa.domaintrack.core.domain.model.Settings

fun ThemeMode.toDomain() = when(this) {
    ThemeMode.THEME_MODE_AUTO -> AppThemeMode.AUTO
    ThemeMode.THEME_MODE_DARK -> AppThemeMode.DARK
    ThemeMode.THEME_MODE_LIGHT -> AppThemeMode.LIGHT
    is ThemeMode.Unrecognized -> AppThemeMode.AUTO
}

fun AppThemeMode.toProto() = when(this) {
    AppThemeMode.AUTO -> ThemeMode.THEME_MODE_AUTO
    AppThemeMode.LIGHT -> ThemeMode.THEME_MODE_LIGHT
    AppThemeMode.DARK -> ThemeMode.THEME_MODE_DARK

}

fun AlertConfig.toDomain() = AlertSettings(
    redLevel = red_level ?: 7,
    yellowLevel = yellow_level ?: 30,
)

fun AlertSettings.toProto() = AlertConfig(
    red_level = redLevel,
    yellow_level = yellowLevel,
)
