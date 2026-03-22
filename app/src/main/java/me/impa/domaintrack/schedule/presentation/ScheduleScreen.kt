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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.impa.domaintrack.R
import me.impa.domaintrack.core.presentation.SimpleLoading
import me.impa.domaintrack.core.presentation.navigation.BasicDialogScaffold
import me.impa.domaintrack.core.util.formatTime
import me.impa.domaintrack.schedule.presentation.state.ScheduleAction
import me.impa.domaintrack.schedule.presentation.state.ScheduleState
import me.impa.domaintrack.schedule.presentation.state.ScheduleTimeMode
import me.impa.domaintrack.ui.theme.DomainTrackTheme

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.isLoading)
        SimpleLoading()
    else
        ScheduleScreenContent(state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreenContent(state: ScheduleState, onAction: (ScheduleAction) -> Unit = {}) {
    BasicDialogScaffold(
        title = { Text(text = stringResource(R.string.title_schedule)) },
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onAction(ScheduleAction.Cancel) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
                Button(onClick = { onAction(ScheduleAction.Save) }) {
                    Text(text = stringResource(R.string.action_save))
                }
            }
        }
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)) {
            ToggleButton(
                checked = state.timeMode == ScheduleTimeMode.START,
                onCheckedChange = { if (it) onAction(ScheduleAction.SetMode(ScheduleTimeMode.START)) },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Start")
                    Text(text = formatTime(state.startHour, state.startMinute))
                }
            }
            ToggleButton(
                checked = state.timeMode == ScheduleTimeMode.END,
                onCheckedChange = { if (it) onAction(ScheduleAction.SetMode(ScheduleTimeMode.END)) },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "End")
                    Text(text = formatTime(state.endHour, state.endMinute))
                }
            }
        }
        key(state.timeMode) {
            val timePickerState = rememberTimePickerState(
                initialHour = if (state.timeMode == ScheduleTimeMode.START) state.startHour else state.endHour,
                initialMinute = if (state.timeMode == ScheduleTimeMode.START) state.startMinute else state.endMinute
            )
            LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                onAction(ScheduleAction.SetTime(state.timeMode, timePickerState.hour, timePickerState.minute))
            }

            TimePicker(
                state = timePickerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ScheduleScreenPreview() {
    DomainTrackTheme {
        ScheduleScreenContent(
            state = ScheduleState(
                isLoading = false,
                startHour = 7,
                startMinute = 5,
                endHour = 22,
                endMinute = 50,
                timeMode = ScheduleTimeMode.END
            )
        )
    }
}