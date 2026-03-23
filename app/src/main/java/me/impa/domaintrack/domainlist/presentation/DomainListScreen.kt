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

package me.impa.domaintrack.domainlist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExpandedDockedSearchBarWithGap
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.impa.domaintrack.R
import me.impa.domaintrack.core.presentation.state.AlertLevel
import me.impa.domaintrack.core.presentation.state.CertificateInfoState
import me.impa.domaintrack.core.presentation.state.DomainInfoState
import me.impa.domaintrack.core.presentation.state.ExpiryInfo
import me.impa.domaintrack.core.presentation.util.shimmer
import me.impa.domaintrack.core.util.isCompact
import me.impa.domaintrack.core.util.isScrollingUp
import me.impa.domaintrack.domainlist.presentation.state.DomainListAction
import me.impa.domaintrack.domainlist.presentation.state.SortType
import me.impa.domaintrack.ui.theme.DomainTrackTheme
import me.impa.domaintrack.ui.theme.LocalExtendedColorScheme
import kotlin.time.Duration.Companion.days

@Composable
fun DomainListScreen(
    viewModel: DomainListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (!state.isLoading)
        DomainListScreenContent(
            domains = state.domains, isRefreshing = state.isRefreshing,
            sortType = state.sortType,
            updateTime = state.lastUpdated,
            onAction = viewModel::onAction
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DomainListScreenContent(
    domains: List<DomainInfoState>,
    updateTime: String?,
    isRefreshing: Boolean,
    sortType: SortType,
    onAction: (DomainListAction) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val isFabVisible by listState.isScrollingUp()
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    var filter by rememberSaveable { mutableStateOf("") }

    val filteredDomains = remember(filter, domains, sortType) {
        (if (filter.isBlank()) domains else domains.filter { it.domain.contains(filter, ignoreCase = true) })
            .let { domains ->
                when (sortType) {
                    SortType.NAME -> domains.sortedBy { it.domain }
                    SortType.DAYS_LEFT -> domains.sortedBy { it.expiryInfo.daysLeft }
                }
            }
    }
    val searchHints = remember(domains) { domains.map { it.domain }.sortedBy { it } }

    Scaffold(
        topBar = {
            DomainListTopBar(
                scrollBehavior = scrollBehavior,
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                searchHints = searchHints,
                selectedSortType = sortType,
                onSearch = {
                    filter = it
                    textFieldState.setTextAndPlaceCursorAtEnd(it)
                },
                onAction = onAction
            )
        },
        floatingActionButton = {
            DomainListFab(isFabVisible = isFabVisible, onAction = onAction)
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        DomainListLazyColumn(
            paddingValues = paddingValues,
            listState = listState,
            isRefreshing = isRefreshing,
            domains = domains,
            filteredDomains = filteredDomains,
            updateTime = updateTime,
            onAction = onAction
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DomainListFab(isFabVisible: Boolean, onAction: (DomainListAction) -> Unit) {
    FloatingActionButton(
        onClick = { onAction(DomainListAction.AddDomain) },
        modifier = Modifier.animateFloatingActionButton(
            visible = isFabVisible,
            alignment = Alignment.Center
        )
    ) {
        Icon(painterResource(R.drawable.outline_add_icon), contentDescription = null)
    }
}

@Composable
private fun EmptyDomainList(
    icon: Int,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DomainListLazyColumn(
    paddingValues: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isRefreshing: Boolean,
    updateTime: String?,
    domains: List<DomainInfoState>,
    filteredDomains: List<DomainInfoState>,
    onAction: (DomainListAction) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = { onAction(DomainListAction.Refresh) },
        enabled = domains.isNotEmpty(),
        indicator = {
            LoadingIndicator(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues)
            )
        }
    ) {
        when {
            domains.isEmpty() -> EmptyDomainList(
                icon = R.drawable.ufo_icon,
                title = stringResource(R.string.text_no_domains),
                subtitle = stringResource(R.string.text_add_domain_prompt)
            )

            filteredDomains.isEmpty() -> EmptyDomainList(
                icon = R.drawable.empty_search_icon,
                title = stringResource(R.string.text_no_results),
                subtitle = stringResource(R.string.text_no_results_prompt)
            )

            else -> LazyColumn(
                state = listState,
                contentPadding = paddingValues.plus(PaddingValues(bottom = 16.dp)),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "updateStatus") {
                    UpdateStatusLine(updateTime = updateTime)
                }
                items(filteredDomains, key = { it.id }) { domain ->
                    DomainCard(domain = domain, onSelect = { onAction(DomainListAction.SelectDomain(domain.id)) })
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DomainListTopBar(
    scrollBehavior: SearchBarScrollBehavior,
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    searchHints: List<String>,
    selectedSortType: SortType,
    onSearch: (String) -> Unit,
    onAction: (DomainListAction) -> Unit
) {
    val scope = rememberCoroutineScope()
    val doSearch: (String) -> Unit = remember(searchBarState, onSearch) {
        {
            onSearch(it)
            scope.launch { searchBarState.animateToCollapsed() }
        }
    }
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = doSearch,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                SearchBarLeadingIcon(
                    searchBarState = searchBarState,
                    onExpand = { scope.launch { searchBarState.animateToExpanded() } },
                    onCollapse = { scope.launch { searchBarState.animateToCollapsed() } }
                )
            },
            trailingIcon = {
                SearchBarTrailingIcon(
                    searchBarState = searchBarState,
                    textFieldState = textFieldState,
                    onClearFilter = { onSearch("") }
                )
            },
            placeholder = {
                SearchBarPlaceholder()
            }
        )
    }

    AppBarWithSearch(
        scrollBehavior = scrollBehavior,
        state = searchBarState,
        colors = SearchBarDefaults.appBarWithSearchColors(),
        inputField = inputField,
        actions = {
            SortMenu(
                selectedSortType = selectedSortType,
                onAction = onAction
            )
        }
    )
    if (isCompact()) {
        ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
            DomainSearchList(textFieldState = textFieldState, hints = searchHints, onSearch = doSearch)
        }
    } else {
        ExpandedDockedSearchBarWithGap(state = searchBarState, inputField = inputField) {
            DomainSearchList(textFieldState = textFieldState, hints = searchHints, onSearch = doSearch)
        }
    }
}

private val sortTypes = mapOf(
    SortType.NAME to R.string.text_sort_name,
    SortType.DAYS_LEFT to R.string.text_sort_days_left,
)

@Composable
private fun SortMenu(selectedSortType: SortType, onAction: (DomainListAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.outline_sort_icon), contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sortTypes.forEach { (type, resId) ->
                DropdownMenuItem(
                    text = { Text(stringResource(resId)) },
                    onClick = {
                        onAction(DomainListAction.SetSortType(type))
                        expanded = false
                    },
                    leadingIcon = {
                        RadioButton(selected = type == selectedSortType, onClick = null)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.DomainSearchList(
    textFieldState: TextFieldState, hints: List<String>,
    onSearch: (String) -> Unit
) {
    val searchString = textFieldState.text.toString()

    if (searchString.isEmpty()) {
        DomainSearchEmptyQuery()
        return
    }

    val filteredHints = remember(searchString) {
        hints.filter { it.contains(searchString, ignoreCase = true) }.take(5)
    }

    if (filteredHints.isEmpty()) {
        DomainSearchEmptyResult(query = searchString)
        return
    }

    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
    filteredHints.forEach {
        ListItem(
            onClick = { onSearch(it) },
            leadingContent = {
                Icon(painterResource(R.drawable.outline_search_icon), contentDescription = null)
            },
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = it, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ColumnScope.DomainSearchEmptyResult(query: String) {
    Spacer(modifier = Modifier.height(32.dp))
    Icon(
        painterResource(R.drawable.outline_question_mark_icon),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .size(32.dp)
            .align(Alignment.CenterHorizontally)
    )
    Text(stringResource(R.string.text_search_no_results, query),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp))
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun ColumnScope.DomainSearchEmptyQuery() {
    Spacer(modifier = Modifier.height(32.dp))
    Icon(
        painterResource(R.drawable.outline_search_icon),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .size(32.dp)
            .align(Alignment.CenterHorizontally)
    )
    Text(
        text = stringResource(R.string.text_search_domains),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarLeadingIcon(
    searchBarState: SearchBarState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    Crossfade(
        targetState = searchBarState.currentValue,
        label = "SearchBarLeadingIcon"
    ) { state ->
        when (state) {
            SearchBarValue.Collapsed ->
                IconButton(onClick = onExpand) {
                    Icon(painterResource(R.drawable.outline_search_icon), contentDescription = null)
                }

            SearchBarValue.Expanded ->
                IconButton(onClick = onCollapse) {
                    Icon(
                        painterResource(R.drawable.outline_arrow_back_icon),
                        contentDescription = null
                    )
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarTrailingIcon(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    onClearFilter: () -> Unit
) {
    val isSearchNotEmpty by remember {
        derivedStateOf { textFieldState.text.isNotEmpty() }
    }
    AnimatedVisibility(
        visible = isSearchNotEmpty,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        IconButton(onClick = {
            textFieldState.clearText()
            if (searchBarState.currentValue == SearchBarValue.Collapsed)
                onClearFilter()
        }) {
            Icon(
                painterResource(R.drawable.outline_close_icon),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarPlaceholder() {
    Text(
        modifier = Modifier
            .clearAndSetSemantics {},
        text = stringResource(R.string.text_search)
    )
}

@Suppress("MagicNumber")
@Composable
private fun LazyItemScope.DomainCard(domain: DomainInfoState, onSelect: () -> Unit = {}) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize()
            .animateItem()
            .clickable(enabled = true, onClick = onSelect)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            DomainInfoColumn(domain = domain)
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            SslColumn(certificates = domain.certificates)
        }
    }
}

@Composable
private fun RowScope.DomainInfoColumn(domain: DomainInfoState) {
    Column(
        modifier = Modifier
            .weight(0.7f)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        DomainNameText(domainName = domain.domain)
        Box {
            DomainDetails(domain = domain, modifier = Modifier.visible(!domain.noInfo))
            NoDomainInfo(isLoading = domain.isUpdating, modifier = Modifier.visible(domain.noInfo))
        }
    }
}

@Composable
private fun DomainDetails(domain: DomainInfoState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DomainRegistrarText(registrar = domain.registrar, isUpdating = domain.isUpdating)
        Spacer(modifier = Modifier.height(24.dp))
        Box {
            Column(modifier = Modifier.visible(domain.expiryInfo.expirationDate != 0L)) {
                DomainExpirationInfo(expiryInfo = domain.expiryInfo, isUpdating = domain.isUpdating)
                DomainExpirationDate(expirationDateMedium = domain.expirationDateMedium, isUpdating = domain.isUpdating)
            }
            NoExpirationInfo(
                isLoading = domain.isUpdating,
                modifier = Modifier.visible(domain.expiryInfo.expirationDate == 0L)
            )
        }
    }
}

@Composable
private fun NoDomainInfo(isLoading: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.text_information_not_available),
        modifier = modifier.then(Modifier.shimmer(minWidth = 96.dp, isLoading = isLoading)),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NoExpirationInfo(isLoading: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.text_expiration_date_unknown),
        modifier = modifier.then(Modifier.shimmer(minWidth = 96.dp, isLoading = isLoading)),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DomainNameText(domainName: String) {
    Text(
        text = domainName.uppercase(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun DomainRegistrarText(registrar: String, isUpdating: Boolean) {
    Text(
        text = registrar,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.shimmer(minWidth = 128.dp, isLoading = isUpdating)
    )
}

@Composable
private fun DomainExpirationInfo(expiryInfo: ExpiryInfo, isUpdating: Boolean) {
    val daysColor = when (expiryInfo.alertLevel) {
        AlertLevel.NONE -> MaterialTheme.colorScheme.onSurface
        AlertLevel.YELLOW -> LocalExtendedColorScheme.current.warningOnSurface
        AlertLevel.RED -> MaterialTheme.colorScheme.error
    }
    Row(modifier = Modifier.shimmer(minWidth = 96.dp, isLoading = isUpdating)) {
        Text(
            text = expiryInfo.daysLeft.toString(),
            modifier = Modifier
                .padding(end = 4.dp)
                .alignByBaseline(),
            fontWeight = FontWeight.Bold,
            color = daysColor,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = pluralStringResource(R.plurals.text_days, expiryInfo.daysLeft),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alignByBaseline()
        )
    }
    Text(
        text = stringResource(R.string.text_until_expiration),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DomainExpirationDate(expirationDateMedium: String, isUpdating: Boolean) {
    Text(
        text = stringResource(R.string.text_expires_on, expirationDateMedium),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .padding(top = 8.dp)
            .shimmer(minWidth = 96.dp, isLoading = isUpdating)
    )
}

@Composable
private fun RowScope.SslColumn(certificates: List<CertificateInfoState>) {
    Column(
        modifier = Modifier
            .weight(0.3f)
            .padding(start = 8.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
    ) {
        certificates.firstOrNull()?.let { SslCard(it) }
    }
}

@Composable
private fun ColumnScope.SslCard(cert: CertificateInfoState) {
    val warnColor = when {
        cert.noCert -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    SslIcon(tint = warnColor)
    SslStatusText(tint = warnColor)
    if (cert.noCert) {
        SslNoCertText(isUpdating = cert.isUpdating)
    } else {
        SslIssuerInfo(issuer = cert.issuer, isUpdating = cert.isUpdating)
        SslExpiryInfo(notAfterMedium = cert.notAfterMedium, isUpdating = cert.isUpdating)
    }
}

@Composable
private fun ColumnScope.SslIcon(tint: Color) {
    Icon(
        painterResource(R.drawable.cert_icon),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(32.dp),
        tint = tint
    )
}

@Composable
private fun ColumnScope.SslStatusText(tint: Color) {
    Text(
        text = stringResource(R.string.text_ssl),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.align(Alignment.CenterHorizontally),
        color = tint
    )
}

@Composable
private fun ColumnScope.SslNoCertText(isUpdating: Boolean) {
    Text(
        text = stringResource(R.string.text_ssl_inactive),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .shimmer(minWidth = 72.dp, isLoading = isUpdating)
            .align(Alignment.CenterHorizontally),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun SslIssuerInfo(issuer: String, isUpdating: Boolean) {
    Spacer(modifier = Modifier.defaultMinSize(minHeight = 16.dp))
    Text(
        text = stringResource(R.string.text_issuer).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = issuer,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.shimmer(minWidth = 72.dp, isLoading = isUpdating),
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun SslExpiryInfo(notAfterMedium: String, isUpdating: Boolean) {
    Spacer(modifier = Modifier.defaultMinSize(minHeight = 8.dp))
    Text(
        text = stringResource(R.string.text_expiry).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = notAfterMedium,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.shimmer(minWidth = 72.dp, isLoading = isUpdating),
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis
    )
}

@PreviewLightDark
@Composable
private fun DomainListScreenContentPreview() {
    DomainTrackTheme {
        DomainListScreenContent(
            domains = sampleDomains, sortType = SortType.NAME,
            updateTime = "12/12/12 13:13",
            isRefreshing = false
        )
    }
}

@PreviewLightDark
@Composable
private fun DomainListScreenContentEmptyPreview() {
    DomainTrackTheme {
        DomainListScreenContent(
            domains = listOf(), sortType = SortType.NAME,
            updateTime = "10/10/1010 12:12",
            isRefreshing = false
        )
    }
}


private val sampleDomains = listOf(
    DomainInfoState(
        id = 0,
        domain = "google.com",
        registrar = "MarkMonitor Inc.",
        expirationDateShort = "2025-09-14",
        checkTimeRaw = 1,
        creationDateShort = "1997-09-15",
        creationDateMedium = "Sep 15, 1997",
        expirationDateMedium = "Sep 14, 2025",
        updatedDateShort = "2023-09-14",
        updatedDateMedium = "Sep 14, 2023",
        notes = "",
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
                    daysLeft = 120,
                    alertLevel = AlertLevel.NONE,
                    expirationDate = System.currentTimeMillis() + 120.days.inWholeMilliseconds
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
                noCert = false,
                expiryInfo = ExpiryInfo(
                    daysLeft = 120,
                    alertLevel = AlertLevel.NONE,
                    expirationDate = System.currentTimeMillis() + 120.days.inWholeMilliseconds
                ),
                checkTimeRaw = System.currentTimeMillis()
            )
        ),
        isUpdating = false,
        noInfo = true,
        expiryInfo = ExpiryInfo(
            daysLeft = 20,
            alertLevel = AlertLevel.YELLOW,
            expirationDate = System.currentTimeMillis() + 20.days.inWholeMilliseconds
        ),
        checkTime = "Just now",
    ),
    DomainInfoState(
        id = 1,
        domain = "example.com",
        registrar = "Example Registrar",
        expirationDateShort = "2024-10-20",
        checkTimeRaw = 1,
        creationDateShort = "1995-08-14",
        creationDateMedium = "Aug 14, 1995",
        expirationDateMedium = "Oct 20, 2024",
        updatedDateShort = "2023-10-20",
        updatedDateMedium = "Oct 20, 2023",
        notes = "This is a note",
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
                    daysLeft = 120,
                    alertLevel = AlertLevel.NONE,
                    expirationDate = System.currentTimeMillis() + 120.days.inWholeMilliseconds
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
                noCert = false,
                expiryInfo = ExpiryInfo(
                    daysLeft = 120,
                    alertLevel = AlertLevel.NONE,
                    expirationDate = System.currentTimeMillis() + 120.days.inWholeMilliseconds
                ),
                checkTimeRaw = System.currentTimeMillis()
            )
        ),
        isUpdating = false,
        noInfo = false,
        expiryInfo = ExpiryInfo(
            daysLeft = 120,
            alertLevel = AlertLevel.NONE,
            expirationDate = System.currentTimeMillis() + 120.days.inWholeMilliseconds
        ),
        checkTime = "Yesterday",
    )
)
