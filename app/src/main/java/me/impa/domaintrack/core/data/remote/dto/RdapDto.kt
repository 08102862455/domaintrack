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

package me.impa.domaintrack.core.data.remote.dto

import java.util.Date

data class RdapDto(
    val entities: List<EntityDto>? = null,
    val events: List<EventDto>? = null
)

data class EventDto(
    val eventAction: String? = null,
    val eventDate: Date? = null
)

data class EntityDto(
    val objectClassName: String? = null,
    val roles: List<String>? = null,
    val vcardArray: VCardArrayDto? = null
)

data class VCardArrayDto(
    val kind: String? = null,
    val properties: List<VCardPropertyDto>? = null
)

data class VCardPropertyDto(
    val name: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val valueType: String? = null,
    val value: String? = null
)

fun VCardArrayDto.findProperty(name: String): VCardPropertyDto? {
    return properties?.firstOrNull { it.name == name }
}

val VCardArrayDto.fullName: String?
    get() = findProperty("fn")?.value.takeIf { it?.isNotBlank() == true }