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

package me.impa.domaintrack.core.data.remote.serialize

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import me.impa.domaintrack.core.data.remote.dto.VCardArrayDto
import me.impa.domaintrack.core.data.remote.dto.VCardPropertyDto
import java.lang.reflect.Type

class VCardArrayDeserializer: JsonDeserializer<VCardArrayDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VCardArrayDto {
        val array = json.asJsonArray

        require(array.size() == 2) { "vcardArray must have exactly 2 elements" }

        val kind = array[0].asJsonPrimitive.asString
        val propertiesArray = array[1].asJsonArray

        val properties = propertiesArray.map { element ->
            deserializeProperty(element.asJsonArray)
        }

        return VCardArrayDto(kind, properties)

    }

    private fun deserializeProperty(prop: JsonArray): VCardPropertyDto {
        require(prop.size() == 4) { "Property must have exactly 4 elements" }

        val name = try { prop[0].asJsonPrimitive.asString } catch (_: IllegalStateException) { "ERR" }
        val parameters = extractParameters(prop[1])
        val valueType = try { prop[2].asJsonPrimitive.asString } catch (_: IllegalStateException) { "ERR" }
        val value = try { prop[3].asJsonPrimitive.asString } catch (_: IllegalStateException) { "ERR" }

        return VCardPropertyDto(name, parameters, valueType, value)
    }

    private fun extractParameters(element: JsonElement): Map<String, String> {
        return when {
            element.isJsonObject -> {
                element.asJsonObject.asMap().mapValues { (_, v) ->
                    if (v.isJsonPrimitive) v.asJsonPrimitive.asString else v.toString()
                }
            }
            element.isJsonNull || (element.isJsonPrimitive && element.asJsonPrimitive.asString.isEmpty()) -> {
                emptyMap()
            }
            else -> emptyMap()
        }
    }
}