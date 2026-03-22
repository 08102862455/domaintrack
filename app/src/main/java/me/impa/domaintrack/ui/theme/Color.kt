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

import androidx.compose.ui.graphics.Color

val SeedColor = Color(0xFF4AA4FF)

interface CustomColorScheme {
    val noErrorOnSurface: Color
    val noErrorContainer: Color
    val noErrorOnContainer: Color
    val warningOnSurface: Color
    val warningContainer: Color
    val warningOnContainer: Color
    val errorOnSurface: Color
    val errorContainer: Color
    val errorOnContainer: Color
}

val CustomColorSchemeLight = object : CustomColorScheme {
    override val noErrorOnSurface = Color(0xFF2E7D32)
    override val noErrorContainer = Color(0xFFC8E6C9)
    override val noErrorOnContainer = Color(0xFF1B5E20)
    override val warningOnSurface = Color(0xFFD84315)
    override val warningContainer = Color(0xFFFFE0B2)
    override val warningOnContainer = Color(0xFFBF360C)
    override val errorOnSurface = Color(0xFFB71C1C)
    override val errorContainer = Color(0xFFFF9B70)
    override val errorOnContainer = Color(0xFF820400)
}

val CustomColorSchemeDark = object : CustomColorScheme {
    override val noErrorOnSurface = Color(0xFF81C784)
    override val noErrorContainer = Color(0xFF388E3C)
    override val noErrorOnContainer = Color(0xFFFFFFFF)
    override val warningOnSurface = Color(0xFFFFB74D)
    override val warningContainer = Color(0xFF965E04)
    override val warningOnContainer = Color(0xFFFFBC8C)
    override val errorOnSurface = Color(0xFFFF6B6B)
    override val errorContainer = Color(0xFF7F1D1D)
    override val errorOnContainer = Color(0xFFFED7AA)
}