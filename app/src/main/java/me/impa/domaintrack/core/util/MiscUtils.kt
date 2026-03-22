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

package me.impa.domaintrack.core.util

fun getFullDomain(domain: String, subdomain: String) =
    (if (subdomain.isEmpty()) "" else "${subdomain}.") + domain

private val countryFlagCache = mutableMapOf<String, String>()

fun getCountryFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return ""
    
    return countryFlagCache.getOrPut(countryCode) {
        val base = 0x1F1E6 // Regional Indicator Symbol Letter A
        val first = base + (countryCode[0].uppercaseChar() - 'A')
        val second = base + (countryCode[1].uppercaseChar() - 'A')
        
        if (first in 0x1F1E6..0x1F1FF && second in 0x1F1E6..0x1F1FF) {
            String(Character.toChars(first)) + String(Character.toChars(second))
        } else {
            ""
        }
    }
}