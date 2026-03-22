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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.alorma.compose.settings.ui.expressive.SettingsButtonGroup
import com.alorma.compose.settings.ui.expressive.SettingsGroup
import com.alorma.compose.settings.ui.expressive.SettingsMenuLink
import com.alorma.compose.settings.ui.expressive.SettingsSwitch
import me.impa.domaintrack.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.impa.domaintrack.R
import me.impa.domaintrack.core.domain.model.AppThemeMode
import me.impa.domaintrack.core.domain.model.Settings
import me.impa.domaintrack.core.util.formatTime
import me.impa.domaintrack.core.util.isNextDaySchedule
import me.impa.domaintrack.settings.presentation.state.SettingsAction
import me.impa.domaintrack.settings.presentation.state.SettingsState
import me.impa.domaintrack.ui.theme.DomainTrackTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (!state.isLoading)
        SettingsScreenContent(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun SettingsScreenContent(state: SettingsState, onAction: (SettingsAction) -> Unit) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(listState)
    val isBatteryOptimizationDisabled = remember { isBatteryOptimizationDisabled(context) }

    Scaffold(
        topBar = {
            SettingsTopAppBar(scrollBehavior = scrollBehavior)
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
            item(key = "monitoring_settings") {
                MonitoringSettings(
                    monitoringEnabled = state.settings.monitoring.enableMonitoring,
                    scheduleHourStart = state.settings.monitoring.scheduleTimeStart / 60,
                    scheduleMinuteStart = state.settings.monitoring.scheduleTimeStart % 60,
                    scheduleHourEnd = state.settings.monitoring.scheduleTimeEnd / 60,
                    scheduleMinuteEnd = state.settings.monitoring.scheduleTimeEnd % 60,
                    notificationEnabled = state.settings.notificationEnabled,
                    isBatteryOptimizationDisabled = isBatteryOptimizationDisabled,
                    nextRunTime = state.nextRunTime,
                    onAction = onAction
                )
            }
            item(key = "appearance_settings") {
                AppearanceSettings(
                    themeMode = state.settings.theme,
                    amoled = state.settings.amoled,
                    onAction = onAction
                )
            }
            item(key = "about") {
                AboutSettings()
            }
        }
    }
}

private fun isBatteryOptimizationDisabled(context: Context): Boolean {
    return try {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun SettingsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(text = stringResource(R.string.title_navigation_settings)) },
    )
}

private val themeModeList = mapOf(
    AppThemeMode.AUTO to R.string.text_auto,
    AppThemeMode.LIGHT to R.string.text_light,
    AppThemeMode.DARK to R.string.text_dark
)

private val themeModeKeyList = themeModeList.keys.toList()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppearanceSettings(
    themeMode: AppThemeMode, amoled: Boolean,
    onAction: (SettingsAction) -> Unit
) {

    val resources = LocalResources.current
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    SettingsGroup(
        title = {
            Text(
                stringResource(R.string.text_appearance),
                color = MaterialTheme.colorScheme.primary
            )
        },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        SettingsButtonGroup(
            title = { Text(stringResource(R.string.text_theme)) },
            items = themeModeKeyList,
            selectedItem = themeMode,
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(0, 2),
            onItemSelected = { onAction(SettingsAction.SetTheme(it)) },
            itemTitleMap = { resources.getString(requireNotNull(themeModeList[it])) }
        )
        SettingsSwitch(
            state = amoled,
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(1, 2),
            title = { Text(stringResource(R.string.text_oled_dark_mode)) },
            subtitle = { Text(stringResource(R.string.text_oled_dark_mode_summary)) },
            onCheckedChange = { onAction(SettingsAction.SetAmoled(it)) }
        )
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalPermissionsApi::class)
@Composable
private fun MonitoringSettings(
    monitoringEnabled: Boolean,
    scheduleHourStart: Int,
    scheduleMinuteStart: Int,
    scheduleHourEnd: Int,
    scheduleMinuteEnd: Int,
    notificationEnabled: Boolean,
    isBatteryOptimizationDisabled: Boolean,
    nextRunTime: String?,
    onAction: (SettingsAction) -> Unit
) {
    val context = LocalContext.current
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(notificationEnabled, monitoringEnabled) {
            if (!notificationPermission.status.isGranted && notificationEnabled && monitoringEnabled)
                notificationPermission.launchPermissionRequest()
        }
    }

    SettingsGroup(
        title = {
            Text(
                stringResource(R.string.text_monitoring),
                color = MaterialTheme.colorScheme.primary
            )
        },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        SettingsSwitch(
            state = monitoringEnabled,
            title = { Text(stringResource(R.string.text_monitoring_enabled)) },
            subtitle = { Text(stringResource(R.string.text_monitoring_enabled_summary)) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(0, 6),
            onCheckedChange = { onAction(SettingsAction.SetMonitoring(it)) }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_schedule)) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(1, 6),
            action = { Icon(painterResource(R.drawable.outline_calendar_clock_icon), contentDescription = null) },
            subtitle = {
                Column {
                    val isNextDay = isNextDaySchedule(
                        scheduleHourStart, scheduleMinuteStart,
                        scheduleHourEnd, scheduleMinuteEnd
                    )
                    val formatRes = if (isNextDay) R.string.text_schedule_format_next_day
                    else R.string.text_schedule_format
                    Text(
                        text = stringResource(
                            formatRes,
                            formatTime(scheduleHourStart, scheduleMinuteStart),
                            formatTime(scheduleHourEnd, scheduleMinuteEnd)
                        )
                    )
                    Text(
                        text = nextRunTime?.let {
                            stringResource(R.string.text_next_run, it)
                        } ?: stringResource(R.string.text_not_scheduled),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            onClick = { onAction(SettingsAction.EditSchedule) }
        )
        SettingsSwitch(
            state = notificationEnabled && monitoringEnabled,
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(2, 6),
            title = { Text(stringResource(R.string.text_receive_notifications)) },
            subtitle = { Text(stringResource(R.string.text_receive_notifications_summary)) },
            enabled = monitoringEnabled,
            onCheckedChange = { onAction(SettingsAction.SetNotificationEnabled(it)) }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_alert_rules_settings)) },
            subtitle = { Text(stringResource(R.string.text_alert_rules_settings_summary)) },
            colors = colors,
            action = { Icon(painterResource(R.drawable.outline_chevron_right_icon), contentDescription = null) },
            shapes = ListItemDefaults.segmentedShapes(3, 6),
            onClick = { onAction(SettingsAction.NavigateToAlertRules) }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_system_notification_settings)) },
            subtitle = { Text(stringResource(R.string.text_system_notification_settings_summary)) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(4, 6),
            action = { Icon(painterResource(R.drawable.outline_arrow_forward_icon), contentDescription = null) },
            onClick = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                }
                context.startActivity(intent)
            }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_battery_optimization_title)) },
            subtitle = {
                Text(
                    text = stringResource(
                        if (isBatteryOptimizationDisabled) {
                            R.string.text_battery_optimization_status_disabled
                        } else {
                            R.string.text_battery_optimization_status_enabled
                        }
                    )
                )
            },
            action = {
                Icon(painterResource(R.drawable.outline_chevron_right_icon), contentDescription = null)
            },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(5, 6),
            onClick = { onAction(SettingsAction.NavigateToBatteryOptimization) }
        )
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutSettings() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val packageName = context.packageName
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    SettingsGroup(
        title = {
            Text(
                stringResource(R.string.title_about),
                color = MaterialTheme.colorScheme.primary
            )
        },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_version)) },
            subtitle = { Text(BuildConfig.VERSION_NAME) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(0, 4),
            onClick = { }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_privacy_policy)) },
            subtitle = { Text(stringResource(R.string.text_privacy_policy_summary)) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(1, 4),
            action = { Icon(painterResource(R.drawable.outline_arrow_forward_icon), contentDescription = null) },
            onClick = { uriHandler.openUri("https://impalex.github.io/domaintrack/privacy_policy.html") }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_rate_app)) },
            subtitle = { Text(stringResource(R.string.text_rate_app_summary)) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(2, 4),
            action = { Icon(painterResource(R.drawable.outline_arrow_forward_icon), contentDescription = null) },
            onClick = { uriHandler.openUri("market://details?id=$packageName") }
        )
        SettingsMenuLink(
            title = { Text(stringResource(R.string.text_source_code)) },
            subtitle = { Text(stringResource(R.string.text_source_code_summary)) },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(3, 4),
            action = { Icon(painterResource(R.drawable.outline_arrow_forward_icon), contentDescription = null) },
            onClick = { uriHandler.openUri("https://github.com/impalex/domaintrack") }
        )
    }
}

@PreviewLightDark
@Composable
private fun AppearanceSettingsPreview() {
    DomainTrackTheme {
        Surface {
            SettingsScreenContent(
                state = SettingsState(
                    isLoading = false,
                    settings = Settings.DEFAULT
                ),
                onAction = {}
            )
        }
    }
}
