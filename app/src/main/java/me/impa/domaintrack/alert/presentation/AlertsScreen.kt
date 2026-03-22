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

package me.impa.domaintrack.alert.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.impa.domaintrack.R
import me.impa.domaintrack.alert.domain.model.AlertEntity
import me.impa.domaintrack.alert.presentation.state.AlertsAction
import me.impa.domaintrack.alert.presentation.state.AlertsState
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.DomainInfo
import me.impa.domaintrack.core.presentation.state.AlertLevel
import me.impa.domaintrack.ui.theme.DomainTrackTheme
import me.impa.domaintrack.ui.theme.LocalExtendedColorScheme
import kotlin.time.Duration.Companion.days

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AlertsScreenContent(state, viewModel::onAction)
}

@Composable
private fun EmptyAlertsList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.no_alert_icon),
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.text_all_clear),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.text_no_upcoming_expirations),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun AlertsScreenContent(state: AlertsState, onAction: (AlertsAction) -> Unit) {
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(listState)
    Scaffold(
        topBar = {
            AlertsTopAppBar(scrollBehavior = scrollBehavior)
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPaddings ->
        val hasAlerts = state.redAlerts.isNotEmpty() || state.yellowAlerts.isNotEmpty()
        
        if (!hasAlerts) {
            EmptyAlertsList()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPaddings.plus(PaddingValues(bottom = 16.dp)),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                if (state.redAlerts.isNotEmpty()) {
                    item(key = "red_alerts") {
                        Text(
                            text = stringResource(R.string.text_notification_red_alerts),
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    itemsIndexed(
                        state.redAlerts,
                        key = { _, alert -> "${alert::class.java}_${alert.subject}" }) { index, alert ->
                        AlertItem(index, state.redAlerts.size, alert, onAction)
                    }
                }
                if (state.yellowAlerts.isNotEmpty()) {
                    item(key = "yellow_alerts") {
                        Text(
                            text = stringResource(R.string.text_notification_yellow_alerts),
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    itemsIndexed(
                        state.yellowAlerts,
                        key = { _, alert -> "${alert::class.java}_${alert.subject}" }) { index, alert ->
                        AlertItem(index, state.yellowAlerts.size, alert, onAction)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AlertsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(text = stringResource(R.string.title_navigation_alerts)) },
    )
}

fun getIconId(alert: AlertEntity) = when {
    alert is AlertEntity.Domain && alert.domain.noInfo -> R.drawable.outline_question_mark_icon
    alert is AlertEntity.Certificate && alert.certificate.noCert -> R.drawable.outline_shield_question_icon
    alert.level == AlertLevel.RED -> R.drawable.outline_error_icon
    alert.level == AlertLevel.YELLOW -> R.drawable.outline_warning_icon
    else -> R.drawable.outline_warning_icon
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlertItem(
    index: Int, size: Int, alert: AlertEntity,
    onAction: (AlertsAction) -> Unit
) {
    val iconId = getIconId(alert)
    ListItem(
        onClick = { onAction(AlertsAction.OpenDomain(alert.domainId)) },
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shapes = ListItemDefaults.segmentedShapes(index, size),
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (alert.level) {
                    AlertLevel.NONE -> Color.Unspecified
                    AlertLevel.YELLOW -> LocalExtendedColorScheme.current.warningContainer
                    AlertLevel.RED -> LocalExtendedColorScheme.current.errorContainer
                },
                contentColor = when (alert.level) {
                    AlertLevel.NONE -> Color.Unspecified
                    AlertLevel.YELLOW -> LocalExtendedColorScheme.current.onWarningContainer
                    AlertLevel.RED -> LocalExtendedColorScheme.current.onErrorContainer
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = when (alert.level) {
                        AlertLevel.NONE -> Color.Unspecified
                        AlertLevel.YELLOW -> LocalExtendedColorScheme.current.warningOutline
                        AlertLevel.RED -> LocalExtendedColorScheme.current.errorOutline
                    }
                )
            ) {
                Icon(
                    painterResource(iconId),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                )
            }
        },
        overlineContent = {
            Text(
                text = stringResource(
                    when (alert) {
                        is AlertEntity.Certificate -> R.string.text_notification_cert_overline
                        is AlertEntity.Domain -> R.string.text_notification_domain_overline
                    }
                ).uppercase()
            )
        },
        supportingContent = {
            Text(text = getAlertText(alert))
        },
        content = {
            Text(text = alert.subject, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    )

}

@Composable
private fun getAlertText(alert: AlertEntity) = when (alert) {
    is AlertEntity.Certificate if alert.certificate.noCert ->
        stringResource(R.string.text_notification_cert_not_found)

    is AlertEntity.Certificate -> pluralStringResource(
        R.plurals.text_notification_cert,
        count = alert.daysLeft,
        alert.expiration,
        alert.daysLeft
    )

    is AlertEntity.Domain if alert.domain.noInfo ->
        stringResource(R.string.text_notification_domain_not_found)

    is AlertEntity.Domain -> pluralStringResource(
        R.plurals.text_notification_domain,
        count = alert.daysLeft,
        alert.expiration,
        alert.daysLeft
    )
}

@PreviewLightDark
@Composable
private fun AlertsScreenContentPreview() {
    DomainTrackTheme {
        Surface {
            AlertsScreenContent(
                state = AlertsState(
                    redAlerts = listOf(
                        AlertEntity.Domain(
                            DomainInfo.EMPTY.copy(
                                domain = "example.com",
                                noInfo = true,
                                expirationDate = System.currentTimeMillis() + 2.days.inWholeMilliseconds
                            ),
                            level = AlertLevel.RED
                        ),
                        AlertEntity.Certificate(
                            CertificateInfo.EMPTY.copy(
                                subdomain = "some",
                                notAfter = System.currentTimeMillis() + 1.days.inWholeMilliseconds
                            ), domain = "example.com",
                            level = AlertLevel.RED
                        ),
                        AlertEntity.Domain(
                            DomainInfo.EMPTY.copy(
                                domain = "google.com",
                                expirationDate = System.currentTimeMillis() + 5.days.inWholeMilliseconds
                            ),
                            level = AlertLevel.RED
                        ),
                    ),
                    yellowAlerts = listOf(
                        AlertEntity.Domain(
                            DomainInfo.EMPTY.copy(
                                domain = "yandex.ru",
                                expirationDate = System.currentTimeMillis() + 25.days.inWholeMilliseconds
                            ),
                            level = AlertLevel.YELLOW
                        ),
                        AlertEntity.Certificate(
                            CertificateInfo.EMPTY.copy(
                                subdomain = "mail",
                                notAfter = System.currentTimeMillis() + 20.days.inWholeMilliseconds
                            ), domain = "mail.ru",
                            level = AlertLevel.YELLOW
                        ),
                    )
                ),
                onAction = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AlertsScreenContentEmptyPreview() {
    DomainTrackTheme {
        Surface {
            AlertsScreenContent(
                state = AlertsState(
                    redAlerts = emptyList(),
                    yellowAlerts = emptyList(),

                    ),
                onAction = {},
            )
        }
    }
}