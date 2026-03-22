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

package me.impa.domaintrack.domainedit.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import me.impa.domaintrack.core.presentation.navigation.BottomSheetDialogScaffold
import me.impa.domaintrack.domainedit.presentation.state.DomainEditAction
import androidx.compose.ui.tooling.preview.Preview
import me.impa.domaintrack.R
import me.impa.domaintrack.ui.theme.DomainTrackTheme

@Composable
fun DomainEditScreen(viewModel: DomainEditViewModel) {
    val state by viewModel.state.collectAsState()
    DomainEditScreenContent(
        domain = state.domainName,
        isDomainNameValid = state.isDomainNameValid,
        isAddDomain = state.isAddDomain,
        onIntent = viewModel::onAction
    )
}

private val keyboardOptions = KeyboardOptions.Default.copy(
    keyboardType = KeyboardType.Uri,
    autoCorrectEnabled = false
)

@Composable
private fun DomainEditScreenContent(
    domain: String,
    isDomainNameValid: Boolean,
    isAddDomain: Boolean,
    onIntent: (DomainEditAction) -> Unit = {}
) {
    BottomSheetDialogScaffold(
        title = stringResource(if (isAddDomain) R.string.title_add_domain else R.string.title_edit_domain),
        subtext = stringResource(R.string.text_enter_domain_to_monitor),
        onSave = { onIntent(DomainEditAction.SaveDomain) },
        onCancel = { onIntent(DomainEditAction.Cancel) },
        isSaveEnabled = isDomainNameValid
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = domain,
            onValueChange = { onIntent(DomainEditAction.SetDomain(it)) },
            label = { Text(stringResource(R.string.text_domain_name)) },
            placeholder = { Text(stringResource(R.string.text_example_com)) },
            singleLine = true,
            keyboardOptions = keyboardOptions,
        )
    }
}

@Preview
@Composable
private fun DomainEditScreenContentPreview() {
    DomainTrackTheme {
        DomainEditScreenContent(
            domain = "example.com",
            isAddDomain = true,
            isDomainNameValid = true
        )
    }
}
