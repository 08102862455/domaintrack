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

package me.impa.domaintrack.domainview.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.impa.domaintrack.R
import me.impa.domaintrack.core.presentation.state.AlertLevel
import me.impa.domaintrack.core.presentation.state.CertificateInfoState
import me.impa.domaintrack.core.presentation.state.DomainInfoState
import me.impa.domaintrack.core.presentation.state.ExpiryInfo
import me.impa.domaintrack.core.presentation.util.shimmer
import me.impa.domaintrack.domainview.presentation.state.DomainViewAction
import me.impa.domaintrack.ui.theme.DomainTrackTheme
import me.impa.domaintrack.ui.theme.LocalExtendedColorScheme
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DomainViewScreen(viewModel: DomainViewViewModel) {
    val state by viewModel.state.collectAsState()

    state.domain?.let {
        DomainViewScreenContent(
            domainInfo = it,
            isRefreshing = state.isRefreshing,
            onAction = viewModel::onAction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DomainViewScreenContent(
    domainInfo: DomainInfoState,
    isRefreshing: Boolean,
    onAction: (DomainViewAction) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(listState)
    val pullToRefreshState = rememberPullToRefreshState()
    val isExpirationDateUnknown = domainInfo.expiryInfo.expirationDate == 0L && !domainInfo.noInfo

    Scaffold(
        topBar = {
            DomainViewTopAppBar(scrollBehavior = scrollBehavior, onAction = onAction)
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPaddings ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = { onAction(DomainViewAction.Refresh) },
            indicator = {
                LoadingIndicator(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(innerPaddings)
                )
            }
        ) {
            LazyColumn(
                contentPadding = innerPaddings.plus(PaddingValues(bottom = 16.dp)),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "update_status") {
                    UpdateStatusLine(updateTime = domainInfo.checkTime)
                }
                item(key = "domain-hdr") {
                    IconHeader(R.drawable.domain_icon, domainInfo.domain)
                }
                item(key = "domain-remains") {
                    DomainStatusBox(
                        daysLeft = domainInfo.expiryInfo.daysLeft,
                        alertLevel = domainInfo.expiryInfo.alertLevel,
                        isUpdating = domainInfo.isUpdating,
                        isExpirationDateUnknown = isExpirationDateUnknown,
                        isNoInfo = domainInfo.noInfo
                    )
                }
                item(key = "domain-expiration-info") {
                    AnimatedVisibility(
                        visible = isExpirationDateUnknown && !domainInfo.isUpdating && !domainInfo.noInfo,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ExpirationDateUnknownCard()
                    }
                }
                item(key = "domain-info") {
                    AnimatedVisibility(
                        visible = !domainInfo.noInfo,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        DomainInfoCard(domainInfo = domainInfo)
                    }
                }
                item(key = "cert-hdr") {
                    IconHeader(R.drawable.cert_icon, "SSL Certificates")
                }
                items(items = domainInfo.certificates, key = { i -> "cert-${i.id}" }) { cert ->
                    CertificateInfoCard(certificateInfo = cert)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun DomainViewTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onAction: (DomainViewAction) -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(text = stringResource(R.string.title_domain_details)) },
        navigationIcon = {
            IconButton(
                onClick = { onAction(DomainViewAction.GoBack) },
                modifier = Modifier.visible(LocalListDetailSceneScope.current == null)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_icon),
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = { onAction(DomainViewAction.Edit) }) {
                Icon(painter = painterResource(R.drawable.outline_edit_icon), contentDescription = null)
            }
            IconButton(onClick = { onAction(DomainViewAction.Delete) }) {
                Icon(painter = painterResource(R.drawable.outline_delete_icon), contentDescription = null)
            }
        }
    )
}

@Composable
private fun DomainStatusBox(
    daysLeft: Int,
    alertLevel: AlertLevel,
    isUpdating: Boolean,
    isExpirationDateUnknown: Boolean,
    isNoInfo: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when {
            isNoInfo && !isUpdating -> NoInfoBox()
            isExpirationDateUnknown && !isUpdating -> ExpirationDateUnknownBox()
            else -> DaysRemainingBox(daysLeft, alertLevel, isUpdating)
        }
    }
}

private const val BAD_DOMAIN_REPORT =
    "https://docs.google.com/forms/d/e/1FAIpQLSeW4O2LvvB2hdV8HpVJouNnHaIWvk6Ni6taCPXigRfLg8iFqw/viewform"

@Composable
private fun NoInfoBox() {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text(
                text = stringResource(R.string.text_information_not_available),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 64.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = stringResource(R.string.text_no_info_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = { uriHandler.openUri(BAD_DOMAIN_REPORT) }) {
            Text(text = stringResource(R.string.text_report_issue))
        }
    }
}

@Composable
private fun ExpirationDateUnknownBox() {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = stringResource(R.string.text_expiration_date_unknown),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 64.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun DaysRemainingBox(daysLeft: Int, alertLevel: AlertLevel, isUpdating: Boolean) {
    val (backgroundColor, foregroundColor, outlineColor) = when (alertLevel) {
        AlertLevel.NONE -> Triple(
            LocalExtendedColorScheme.current.noErrorContainer,
            LocalExtendedColorScheme.current.onNoErrorContainer,
            LocalExtendedColorScheme.current.noErrorOutline,
        )

        AlertLevel.YELLOW -> Triple(
            LocalExtendedColorScheme.current.warningContainer,
            LocalExtendedColorScheme.current.onWarningContainer,
            LocalExtendedColorScheme.current.warningOutline,
        )

        AlertLevel.RED -> Triple(
            LocalExtendedColorScheme.current.errorContainer,
            LocalExtendedColorScheme.current.onErrorContainer,
            LocalExtendedColorScheme.current.errorOutline,
        )
    }
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp, color = outlineColor,
                shape = RoundedCornerShape(16.dp)
            )
            .shimmer(16.dp, minWidth = 128.dp, isLoading = isUpdating),
        color = backgroundColor,
        contentColor = foregroundColor
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.text_days_remaining,
                daysLeft, daysLeft
            ),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 64.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ExpirationDateUnknownCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_question_mark_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.text_expiration_date_unknown),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.text_expiration_date_unknown_info),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UpdateStatusLine(updateTime: String?) {
    AnimatedVisibility(visible = updateTime != null) {
        updateTime?.let {
            Text(
                text = "Last update: $it",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IconHeader(@DrawableRes icon: Int, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painterResource(icon), contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = text.uppercase(), style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DomainInfoCard(domainInfo: DomainInfoState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        ListRow(
            stringResource(R.string.text_registrar), domainInfo.registrar, domainInfo.isUpdating
        )
        ListRow(
            stringResource(R.string.text_creation_date), domainInfo.creationDateMedium, domainInfo.isUpdating
        )
        ListRow(
            stringResource(R.string.text_updated_date), domainInfo.updatedDateMedium, domainInfo.isUpdating
        )
        ListRow(
            stringResource(R.string.text_expiration_date), domainInfo.expirationDateMedium, domainInfo.isUpdating
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CertificateInfoCard(certificateInfo: CertificateInfoState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        val colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
        val (badgeColorBackground, badgeColorForeground, badgeColorOutline) = when {
            certificateInfo.noCert ->
                with(LocalExtendedColorScheme.current) {
                    Triple(errorContainer, onErrorContainer, errorOutline)
                }

            else ->
                with(LocalExtendedColorScheme.current) {
                    Triple(noErrorContainer, onNoErrorContainer, noErrorOutline)
                }
        }
        ListItem(
            colors = colors,
            leadingContent = {
                Surface(
                    color = badgeColorBackground,
                    contentColor = badgeColorForeground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColorOutline),
                    modifier = Modifier
                        .shimmer(8.dp, isLoading = certificateInfo.isUpdating)
                ) {
                    Icon(
                        painterResource(R.drawable.outline_shield_lock_icon), contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            headlineContent = { Text(text = certificateInfo.fullDomain) }
        )

        if (certificateInfo.noCert) {
            ListRow(label = "", value = "No SSL certificate found", isUpdating = certificateInfo.isUpdating)
        } else {
            ListRow("Issuer", certificateInfo.issuer, certificateInfo.isUpdating)
            ListRow("Valid From", certificateInfo.notBeforeMedium, certificateInfo.isUpdating)
            ListRow("Valid To", certificateInfo.notAfterMedium, certificateInfo.isUpdating)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ListRow(label: String, value: String, isUpdating: Boolean) {
    val colors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
    ListItem(
        colors = colors,
        overlineContent = { Text(text = label.uppercase()) },
        headlineContent = {
            Text(
                text = value,
                modifier = Modifier.shimmer(minWidth = 96.dp, isLoading = isUpdating)
            )
        },
    )
}


@PreviewLightDark
@Composable
private fun DomainViewScreenContentPreview() {
    DomainTrackTheme {
        DomainViewScreenContent(
            domainInfo = sampleDomain,
            isRefreshing = false,
            onAction = {}
        )
    }
}

private val sampleDomain = DomainInfoState(
    id = 0,
    domain = "example.com",
    creationDateShort = "2023-01-01",
    creationDateMedium = "Jan 1, 2023",
    expirationDateShort = "2025-01-01",
    expirationDateMedium = "Jan 1, 2025",
    updatedDateShort = "2024-01-01",
    updatedDateMedium = "Jan 1, 2024",
    registrar = "Example Registrar",
    notes = "These are some notes.",
    checkTime = "2024-07-21 12:00:00",
    checkTimeRaw = System.currentTimeMillis(),
    isUpdating = false,
    noInfo = false,
    expiryInfo = ExpiryInfo(
        daysLeft = 365,
        alertLevel = AlertLevel.NONE,
        expirationDate = System.currentTimeMillis() + 365.days.inWholeMilliseconds
    ),
    certificates = listOf(
        CertificateInfoState(
            id = 0,
            fullDomain = "example.com",
            subdomain = "",
            issuer = "Let's Encrypt",
            country = "US",
            notBeforeShort = "2024-01-01",
            notBeforeMedium = "Jan 1, 2024",
            notAfterShort = "2025-01-01",
            notAfterMedium = "Jan 1, 2025",
            checkTime = "2024-07-21 12:00:00",
            isUpdating = false,
            noCert = false,
            expiryInfo = ExpiryInfo(
                daysLeft = 365,
                alertLevel = AlertLevel.NONE,
                expirationDate = System.currentTimeMillis() + 365.days.inWholeMilliseconds
            ),
            checkTimeRaw = System.currentTimeMillis()
        ),
        CertificateInfoState(
            id = 1,
            fullDomain = "subdomain.example.com",
            subdomain = "subdomain",
            issuer = "Let's Encrypt",
            country = "US",
            notBeforeShort = "2024-01-01",
            notBeforeMedium = "Jan 1, 2024",
            notAfterShort = "2025-01-01",
            notAfterMedium = "Jan 1, 2025",
            checkTime = "2024-07-21 12:00:00",
            isUpdating = false,
            noCert = true,
            expiryInfo = ExpiryInfo(
                daysLeft = 365,
                alertLevel = AlertLevel.NONE,
                expirationDate = System.currentTimeMillis() + 365.days.inWholeMilliseconds
            ),
            checkTimeRaw = System.currentTimeMillis()
        )
    )
)
