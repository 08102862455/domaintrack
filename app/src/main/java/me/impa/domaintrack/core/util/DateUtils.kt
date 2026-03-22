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

import android.icu.util.Calendar
import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun msToDateString(ms: Long, dateFormat: Int = DateFormat.SHORT): String {
    if (ms == 0L) return "N/A"
    val date = Date(ms)
    val formatter = DateFormat.getDateInstance(dateFormat, Locale.getDefault())
    return formatter.format(date)
}

fun msToDateTimeString(ms: Long): String {
    if (ms == 0L) return "N/A"
    val date = Date(ms)
    val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    return formatter.format(date)
}

fun calcDaysLeft(start: Long, end: Long): Int =
    (((end - start) / (1000 * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(0)

fun calcDaysLeft(end: Long): Int = calcDaysLeft(System.currentTimeMillis(), end)

fun splitToHourMinute(value: Int) = value / 60 to value % 60

fun formatTime(hour: Int, minute: Int): String {
    val date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.time
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    return formatter.format(date)
}

fun formatTime(value: Int) = splitToHourMinute(value).let { (hour, minute) -> formatTime(hour, minute) }

/**
 * Checks if a time range crosses midnight.
 * Returns true if the end time is earlier than the start time.
 */
fun isNextDaySchedule(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
    val startTime = startHour * 60 + startMinute
    val endTime = endHour * 60 + endMinute
    return endTime < startTime
}
