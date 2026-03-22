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

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.impa.domaintrack.R
import me.impa.domaintrack.settings.presentation.state.SettingsAction
import me.impa.domaintrack.ui.theme.DomainTrackTheme
import me.impa.domaintrack.ui.theme.LocalExtendedColorScheme

@Composable
fun BatteryOptimizationScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val isBatteryOptimizationDisabled = remember { isBatteryOptimizationDisabled(context) }
    BatteryOptimizationScreenContent(
        isOptimizationDisabled = isBatteryOptimizationDisabled,
        onNavigateBack = { viewModel.onAction(SettingsAction.GoBack) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryOptimizationScreenContent(
    isOptimizationDisabled: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            BatteryOptimizationTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddings)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BatteryOptimizationImage()
            BatteryOptimizationTitle()
            BatteryOptimizationStatus(isOptimizationDisabled)
            BatteryOptimizationDescription()
            BatteryOptimizationInfo()
            BatteryOptimizationButton(
                onClick = {
                    openBatteryOptimizationSettings(context)
                    onNavigateBack()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BatteryOptimizationImage() {
    Icon(
        painter = painterResource(R.drawable.mobile_battery_icon),
        contentDescription = null,
        modifier = Modifier
            .size(96.dp)
            .padding(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun BatteryOptimizationTitle() {
    Text(
        text = stringResource(R.string.text_battery_optimization_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BatteryOptimizationStatus(isOptimizationDisabled: Boolean) {
    val extendedColorScheme = LocalExtendedColorScheme.current
    Text(
        text = stringResource(
            if (isOptimizationDisabled) {
                R.string.text_battery_optimization_status_disabled
            } else {
                R.string.text_battery_optimization_status_enabled
            }
        ),
        style = MaterialTheme.typography.titleMedium,
        color = if (isOptimizationDisabled) {
            extendedColorScheme.noErrorOnSurface
        } else {
            extendedColorScheme.warningOnSurface
        },
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BatteryOptimizationDescription() {
    Text(
        text = stringResource(R.string.text_battery_optimization_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun BatteryOptimizationButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.text_battery_optimization_open_settings),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryOptimizationTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(text = stringResource(R.string.text_battery_optimization_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_icon),
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun BatteryOptimizationInfo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoItem(
            title = stringResource(R.string.text_battery_optimization_item_why_title),
            description = stringResource(R.string.text_battery_optimization_item_why_description)
        )
        InfoItem(
            title = stringResource(R.string.text_battery_optimization_item_what_title),
            description = stringResource(R.string.text_battery_optimization_item_what_description)
        )
        InfoItem(
            title = stringResource(R.string.text_battery_optimization_item_how_title),
            description = stringResource(R.string.text_battery_optimization_item_how_description)
        )
    }
}

@Composable
private fun InfoItem(title: String, description: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun isBatteryOptimizationDisabled(context: Context): Boolean {
    return with(context.getSystemService(Context.POWER_SERVICE) as PowerManager) {
        try {
            isIgnoringBatteryOptimizations(context.packageName)
        } catch (_: Exception) {
            false
        }
    }
}

private fun openBatteryOptimizationSettings(context: Context) {
    try {
        val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Ignore
    }
}

@PreviewLightDark
@Composable
private fun BatteryOptimizationScreenPreview() {
    DomainTrackTheme {
        Surface {
            BatteryOptimizationScreenContent(
                isOptimizationDisabled = false,
                onNavigateBack = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BatteryOptimizationImagePreview() {
    DomainTrackTheme {
        Surface {
            BatteryOptimizationImage()
        }
    }
}

@PreviewLightDark
@Composable
private fun BatteryOptimizationStatusEnabledPreview() {
    DomainTrackTheme {
        Surface {
            BatteryOptimizationStatus(isOptimizationDisabled = false)
        }
    }
}

@PreviewLightDark
@Composable
private fun BatteryOptimizationStatusDisabledPreview() {
    DomainTrackTheme {
        Surface {
            BatteryOptimizationStatus(isOptimizationDisabled = true)
        }
    }
}
