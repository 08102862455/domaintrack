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

package me.impa.domaintrack.schedule.presentation.state

sealed interface ScheduleAction {
    data object Save : ScheduleAction
    data object Cancel : ScheduleAction
    data class SetTime(val mode: ScheduleTimeMode, val hour: Int, val minute: Int) : ScheduleAction
    data class SetMode(val mode: ScheduleTimeMode) : ScheduleAction
}