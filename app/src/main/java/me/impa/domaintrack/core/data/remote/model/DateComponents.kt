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

package me.impa.domaintrack.core.data.remote.model

import java.util.Calendar
import java.util.TimeZone

data class DateComponents(
    val day: Int?,
    val month: Int?,
    val year: Int?,
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null
)

sealed class DatePattern(val regex: Regex) {
    abstract fun extract(match: MatchResult): DateComponents?

    object DatePattern1 : DatePattern(
        ("(?<day>[0-9]{1,2})[./ -](?<month>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)" +
                "[./ -](?<year>[0-9]{4}|[0-9]{2})(\\s+(?<hour>[0-9]{1,2})[:.]" +
                "(?<minute>[0-9]{1,2})[:.](?<second>[0-9]{1,2}))?").toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[1].toIntOrNull(),
                month = monthNameToNumber(match.groupValues[2]),
                year = normalizeYear(match.groupValues[3]),
                hour = match.groupValues[5].toIntOrNull(),
                minute = match.groupValues[6].toIntOrNull(),
                second = match.groupValues[7].toIntOrNull()
            )
        }
    }

    object DatePattern2 : DatePattern(
        ("[a-z]{3}\\s(?<month>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[./ -]" +
                "(?<day>[0-9]{1,2})(\\s+(?<hour>[0-9]{1,2})[:.](?<minute>[0-9]{1,2})[:.]" +
                "(?<second>[0-9]{1,2}))?\\s[a-z]{3}\\s(?<year>[0-9]{4}|[0-9]{2})").toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[2].toIntOrNull(),
                month = monthNameToNumber(match.groupValues[1]),
                year = normalizeYear(match.groupValues[7]),
                hour = match.groupValues[4].toIntOrNull(),
                minute = match.groupValues[5].toIntOrNull(),
                second = match.groupValues[6].toIntOrNull()
            )
        }
    }

    object DatePattern3 : DatePattern(
        ("[a-zA-Z]+\\s(?<day>[0-9]{1,2})(?:st|nd|rd|th)\\s" +
                "(?<month>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|" +
                "March|April|May|June|July|August|September|October|November|December)\\s(?<year>[0-9]{4})")
            .toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[1].toIntOrNull(),
                month = monthNameToNumber(match.groupValues[2]),
                year = normalizeYear(match.groupValues[3])
            )
        }
    }

    object DatePattern4 : DatePattern(
        ("(?<year>[0-9]{4})[./-]?(?<month>[0-9]{2})[./-]?(?<day>[0-9]{2})(\\s|T|/)((?<hour>[0-9]{1,2})[:.-]" +
                "(?<minute>[0-9]{1,2})[:.-](?<second>[0-9]{1,2}))").toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[3].toIntOrNull(),
                month = match.groupValues[2].toIntOrNull(),
                year = normalizeYear(match.groupValues[1]),
                hour = match.groupValues[6].toIntOrNull(),
                minute = match.groupValues[7].toIntOrNull(),
                second = match.groupValues[8].toIntOrNull()
            )
        }
    }

    object DatePattern5 : DatePattern(
        "(?<year>[0-9]{4})[./-](?<month>[0-9]{1,2})[./-](?<day>[0-9]{1,2})".toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[3].toIntOrNull(),
                month = match.groupValues[2].toIntOrNull(),
                year = normalizeYear(match.groupValues[1])
            )
        }
    }

    object DatePattern6 : DatePattern(
        "(?<day>[0-9]{1,2})[./ -](?<month>[0-9]{1,2})[./ -](?<year>[0-9]{4}|[0-9]{2})".toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[1].toIntOrNull(),
                month = match.groupValues[2].toIntOrNull(),
                year = normalizeYear(match.groupValues[3])
            )
        }

    }

    object DatePattern7 : DatePattern(
        "(?<month>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (?<day>[0-9]{1,2}),? (?<year>[0-9]{4})"
            .toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[2].toIntOrNull(),
                month = monthNameToNumber(match.groupValues[1]),
                year = normalizeYear(match.groupValues[3])
            )
        }
    }

    object DatePattern8 : DatePattern(
        ("(?<day>[0-9]{1,2})-(?<month>January|February|March|April|May|June|July|August|" +
                "September|October|November|December)-(?<year>[0-9]{4})").toRegex()
    ) {
        override fun extract(match: MatchResult): DateComponents {
            return DateComponents(
                day = match.groupValues[1].toIntOrNull(),
                month = monthNameToNumber(match.groupValues[2]),
                year = normalizeYear(match.groupValues[3])
            )
        }

    }


    companion object {

        private val allPatterns = listOf(
            DatePattern1,
            DatePattern2,
            DatePattern3,
            DatePattern4,
            DatePattern5,
            DatePattern6,
            DatePattern7,
            DatePattern8
        )

        fun parse(text: String): DateComponents? {
            for (pattern in allPatterns) {
                pattern.regex.find(text)?.let { match ->
                    pattern.extract(match)?.let { return it }
                }
            }
            return null
        }

        private fun monthNameToNumber(name: String): Int? = when (name.lowercase()) {
            "jan", "january" -> 1
            "feb", "february" -> 2
            "mar", "march" -> 3
            "apr", "april" -> 4
            "may" -> 5
            "jun", "june" -> 6
            "jul", "july" -> 7
            "aug", "august" -> 8
            "sep", "september" -> 9
            "oct", "october" -> 10
            "nov", "november" -> 11
            "dec", "december" -> 12
            else -> null
        }

        private fun normalizeYear(year: String): Int? {
            val y = year.toIntOrNull() ?: return null
            return if (y < 100) {
                if (y > 50) 1900 + y else 2000 + y
            } else y
        }
    }
}

fun DateComponents?.toMs(): Long = this?.let { components ->
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(components.year ?: 2000, (components.month ?: 1) - 1, components.day ?: 1,
            components.hour ?: 0, components.minute ?: 0, components.second ?: 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
} ?: 0L