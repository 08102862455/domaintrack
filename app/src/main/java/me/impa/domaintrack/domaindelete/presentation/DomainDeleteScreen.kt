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

package me.impa.domaintrack.domaindelete.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.impa.domaintrack.R
import me.impa.domaintrack.core.domain.model.DomainCertInfo
import me.impa.domaintrack.core.domain.model.DomainInfo
import me.impa.domaintrack.core.domain.model.UpdateState
import me.impa.domaintrack.core.presentation.SimpleLoading
import me.impa.domaintrack.core.presentation.navigation.BasicDialogScaffold
import me.impa.domaintrack.domaindelete.presentation.state.DomainDeleteAction
import me.impa.domaintrack.domaindelete.presentation.state.DomainDeleteState
import me.impa.domaintrack.ui.theme.DomainTrackTheme

@Composable
fun DomainDeleteScreen(viewModel: DomainDeleteViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.isLoading)
        SimpleLoading()
    else
        DomainDeleteScreenContent(state, viewModel::onAction)
}

@Composable
fun DomainDeleteScreenContent(state: DomainDeleteState, onAction: (DomainDeleteAction) -> Unit = {}) {
    BasicDialogScaffold(
        title = { Text(text = stringResource(R.string.title_delete_domain)) },
        actions = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onAction(DomainDeleteAction.Cancel) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = { onAction(DomainDeleteAction.Delete) },
                ) {
                    Text(text = stringResource(R.string.action_delete))
                }
            }
        }
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(), text = stringResource(
                R.string.text_delete_domain_confirm,
                state.domain?.domain?.domain ?: ""
            )
        )
    }
}

@Preview
@Composable
private fun DomainDeleteScreenContentPreview() {
    DomainTrackTheme {
        DomainDeleteScreenContent(
            state = DomainDeleteState(
                domainId = 0,
                isLoading = false,
                domain = DomainCertInfo(
                    domain = DomainInfo(
                        id = 0,
                        domain = "example.com",
                        creationDate = 0L,
                        expirationDate = 0L,
                        updatedDate = 0L,
                        registrar = "Example Registrar",
                        notes = "",
                        checkTime = 0L,
                        updateState = UpdateState.UPDATED,
                        noInfo = false,
                    ),
                    certificates = emptyList()
                )
            )
        )
    }
}