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

package me.impa.domaintrack.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.util.splitToHourMinute
import me.impa.domaintrack.schedule.presentation.state.ScheduleAction
import me.impa.domaintrack.schedule.presentation.state.ScheduleState
import me.impa.domaintrack.schedule.presentation.state.ScheduleTimeMode
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    val navigator: Navigator,
    repository: DomainTrackRepository
) : ViewModel() {
    val state: StateFlow<ScheduleState>
        field = MutableStateFlow(ScheduleState())

    private val settingsManager = repository.settingsManager

    fun onAction(action: ScheduleAction) {
        when (action) {
            ScheduleAction.Cancel -> navigator.goBack()
            ScheduleAction.Save -> viewModelScope.launch {
                settingsManager.setMonitoringSchedule(
                    state.value.startHour * 60 + state.value.startMinute,
                    state.value.endHour * 60 + state.value.endMinute
                )
                navigator.goBack()
            }
            is ScheduleAction.SetTime -> when (action.mode) {
                ScheduleTimeMode.START -> state.update { it.copy(startHour = action.hour, startMinute = action.minute) }
                ScheduleTimeMode.END -> state.update { it.copy(endHour = action.hour, endMinute = action.minute) }
            }

            is ScheduleAction.SetMode -> state.update { it.copy(timeMode = action.mode) }
        }
    }

    init {
        settingsManager.settingsFlow.onEach { settings ->
            val (startHour, startMinute) = splitToHourMinute(settings.monitoring.scheduleTimeStart)
            val (endHour, endMinute) = splitToHourMinute(settings.monitoring.scheduleTimeEnd)
            state.update {
                it.copy(
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }
}