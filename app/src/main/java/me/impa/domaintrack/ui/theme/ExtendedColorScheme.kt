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

package me.impa.domaintrack.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.harmonizeWithPrimary

data class ExtendedColorScheme(
    val warningOnSurface: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val warningOutline: Color,
    val errorOnSurface: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val errorOutline: Color,
    val noErrorOnSurface: Color,
    val noErrorContainer: Color,
    val onNoErrorContainer: Color,
    val noErrorOutline: Color,
)

private const val OUTLINE_DARKEN_RATION = 1.5f

fun ColorScheme.createExtendedColorScheme(isDark: Boolean = false) =
    with(if (isDark) CustomColorSchemeDark else CustomColorSchemeLight) {
        ExtendedColorScheme(
            warningOnSurface = harmonizeWithPrimary(warningOnSurface),
            warningContainer = harmonizeWithPrimary(warningContainer),
            onWarningContainer = harmonizeWithPrimary(warningOnContainer),
            warningOutline = harmonizeWithPrimary(warningContainer).darken(OUTLINE_DARKEN_RATION),
            errorOnSurface = harmonizeWithPrimary(errorOnSurface),
            errorContainer = harmonizeWithPrimary(errorContainer),
            onErrorContainer = harmonizeWithPrimary(errorOnContainer),
            errorOutline = harmonizeWithPrimary(errorContainer).darken(OUTLINE_DARKEN_RATION),
            noErrorOnSurface = harmonizeWithPrimary(noErrorOnSurface),
            noErrorContainer = harmonizeWithPrimary(noErrorContainer),
            onNoErrorContainer = harmonizeWithPrimary(noErrorOnContainer),
            noErrorOutline = harmonizeWithPrimary(noErrorContainer).darken(OUTLINE_DARKEN_RATION),
        )
    }

val LocalExtendedColorScheme = compositionLocalOf<ExtendedColorScheme> {
    error("Extended color scheme is not set")
}
