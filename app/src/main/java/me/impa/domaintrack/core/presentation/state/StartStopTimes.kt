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

package me.impa.domaintrack.core.presentation.state

import android.icu.util.Calendar
import me.impa.domaintrack.core.domain.model.MonitoringSettings

data class StartStopTimes(
    val start: Calendar,
    val stop: Calendar
)

fun MonitoringSettings.toStartStopTimes() = StartStopTimes(
    start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, scheduleTimeStart / 60)
        set(Calendar.MINUTE, scheduleTimeStart % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    },
    stop = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, scheduleTimeEnd / 60)
        set(Calendar.MINUTE, scheduleTimeEnd % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (scheduleTimeEnd<scheduleTimeStart) add(Calendar.DATE, 1)
    }
)