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

package me.impa.domaintrack.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.expressive.SettingsGroup
import me.impa.domaintrack.R
import me.impa.domaintrack.core.domain.model.AlertSettings
import me.impa.domaintrack.core.domain.model.Settings
import me.impa.domaintrack.settings.presentation.state.SettingsAction
import me.impa.domaintrack.settings.presentation.state.SettingsState
import me.impa.domaintrack.ui.theme.DomainTrackTheme
import kotlin.math.roundToInt

@Composable
fun AlertRulesScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    AlertRulesScreenContent(state.settings, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertRulesScreenContent(settings: Settings, onAction: (SettingsAction) -> Unit) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(listState)
    Scaffold(
        topBar = {
            AlertRulesTopAppBar(scrollBehavior = scrollBehavior, onAction = onAction)
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPaddings ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPaddings.plus(PaddingValues(bottom = 16.dp))
        ) {
            item(key = "alert_rules") {
                AlertLevelsSettings(
                    domainConfig = settings.domainAlert,
                    certConfig = settings.certAlert,
                    onAction = onAction
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertRulesTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onAction: (SettingsAction) -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(text = stringResource(R.string.text_alert_rules)) },
        navigationIcon = {
            IconButton(onClick = { onAction(SettingsAction.GoBack) }) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_icon),
                    contentDescription = null
                )
            }
        }
    )
}

private const val CRITICAL_MIN = 2f
private const val CRITICAL_MAX = 10f
private const val CRITICAL_STEPS = (CRITICAL_MAX - CRITICAL_MIN).toInt() - 1
private const val REMINDER_MIN = 11f
private const val REMINDER_MAX = 30f
private const val REMINDER_STEPS = (REMINDER_MAX - REMINDER_MIN).toInt() - 1

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlertLevelsSettings(
    domainConfig: AlertSettings,
    certConfig: AlertSettings,
    onAction: (SettingsAction) -> Unit
) {
    SettingsGroup(
        title = { Text(stringResource(R.string.text_alert_rules)) },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        AlertLevelSliders(
            domainConfig = domainConfig,
            certConfig = certConfig,
            onAction = onAction
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlertLevelSliders(
    domainConfig: AlertSettings,
    certConfig: AlertSettings,
    onAction: (SettingsAction) -> Unit
) {
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
    AlertLevelSlider(
        title = stringResource(R.string.text_domain_critical_alerts),
        value = domainConfig.redLevel,
        range = CRITICAL_MIN..CRITICAL_MAX,
        steps = CRITICAL_STEPS,
        index = 0,
        colors = colors,
        onValueChange = { onAction(SettingsAction.SetDomainRedAlert(it)) }
    )
    AlertLevelSlider(
        title = stringResource(R.string.text_domain_reminder_alerts),
        value = domainConfig.yellowLevel,
        range = REMINDER_MIN..REMINDER_MAX,
        steps = REMINDER_STEPS,
        index = 1,
        colors = colors,
        onValueChange = { onAction(SettingsAction.SetDomainYellowAlert(it)) }
    )
    AlertLevelSlider(
        title = stringResource(R.string.text_cert_critical_alerts),
        value = certConfig.redLevel,
        range = CRITICAL_MIN..CRITICAL_MAX,
        steps = CRITICAL_STEPS,
        index = 2,
        colors = colors,
        onValueChange = { onAction(SettingsAction.SetCertRedAlert(it)) }
    )
    AlertLevelSlider(
        title = stringResource(R.string.text_cert_reminder_alerts),
        value = certConfig.yellowLevel,
        range = REMINDER_MIN..REMINDER_MAX,
        steps = REMINDER_STEPS,
        index = 3,
        colors = colors,
        onValueChange = { onAction(SettingsAction.SetCertYellowAlert(it)) }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlertLevelSlider(
    title: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    index: Int,
    colors: androidx.compose.material3.ListItemColors,
    onValueChange: (Int) -> Unit
) {
    SettingsSlider(
        title = { Text(text = title) },
        subtitle = {
            Text(
                text = pluralStringResource(
                    R.plurals.text_threshold_days,
                    value, value
                )
            )
        },
        colors = colors,
        shape = ListItemDefaults.segmentedShapes(index, 4).shape,
        value = value.toFloat(),
        valueRange = range,
        steps = steps,
        onValueChange = { onValueChange(it.roundToInt()) }
    )
}

@PreviewLightDark
@Composable
private fun AlertRulesScreenPreview() {
    DomainTrackTheme {
        Surface {
            AlertRulesScreenContent(
                settings = Settings.DEFAULT,
                onAction = {}
            )
        }
    }
}
