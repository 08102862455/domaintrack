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

package me.impa.domaintrack.app.presentation

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import me.impa.domaintrack.R
import me.impa.domaintrack.core.domain.model.AppThemeMode
import me.impa.domaintrack.core.presentation.navigation.BottomSheetSceneStrategy
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.core.presentation.navigation.toEntries
import me.impa.domaintrack.core.presentation.state.AppMessage
import me.impa.domaintrack.ui.theme.DomainTrackTheme

@Composable
fun AppScreen(viewModel: AppViewModel = hiltViewModel()) {

    val navigator = viewModel.navigator
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.messageBus.collect {
            showSnackbar(it,  resources, snackbarHostState)
        }
    }

    state.settings?.let { settings ->
        val isDark = when (settings.theme) {
            AppThemeMode.AUTO -> isSystemInDarkTheme()
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }
        DomainTrackTheme(
            darkTheme = isDark,
            isAmoled = settings.amoled
        ) {
            AppNavigationScaffold(
                currentTopLevelKey = navigator.state.currentTopLevelKey as Route,
                alertCount = state.alertCount,
                snackbarHostState = snackbarHostState,
                onNavigate = navigator::navigate
            ) {
                AppNavigationDisplay(
                    navEntries = navigator.state.toEntries(navigationEntryProvider()),
                    onBack = navigator::goBack
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigationScaffold(
    alertCount: Int,
    snackbarHostState: SnackbarHostState,
    currentTopLevelKey: Route = Route.DomainList,
    onNavigate: (NavKey) -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val navigationSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())
    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        navigationSuiteType = navigationSuiteType,
        navigationItems = {
            NavigationSuiteItem(
                selected = currentTopLevelKey is Route.DomainList,
                onClick = { onNavigate(Route.DomainList) },
                icon = {
                    BottomBarIcon(
                        R.drawable.outline_language_icon,
                        R.drawable.outline_language_icon,
                        currentTopLevelKey is Route.DomainList
                    )
                },
                label = { Text(stringResource(R.string.title_navigation_domains)) }
            )
            NavigationSuiteItem(
                selected = currentTopLevelKey is Route.Alerts,
                onClick = { onNavigate(Route.Alerts) },
                icon = {
                    BottomBarIcon(
                        R.drawable.filled_notifications_icon,
                        R.drawable.outline_notifications_icon,
                        currentTopLevelKey is Route.Alerts
                    )
                },
                label = { Text(stringResource(R.string.title_navigation_alerts)) },
                badge = {
                    if (alertCount > 0)
                        Badge { Text(text = alertCount.toString()) }
                }
            )
            NavigationSuiteItem(
                selected = currentTopLevelKey is Route.Settings,
                onClick = { onNavigate(Route.Settings) },
                icon = {
                    BottomBarIcon(
                        R.drawable.filled_settings_icon,
                        R.drawable.outline_settings_icon,
                        currentTopLevelKey is Route.Settings
                    )
                },
                label = { Text(stringResource(R.string.title_navigation_settings)) }
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
            )
        }
    }
}

@Composable
fun BottomBarIcon(selectedIconId: Int, unselectedIconId: Int, selected: Boolean) {

    AnimatedContent(
        targetState = selected,
        transitionSpec = {
            fadeIn() + scaleIn() togetherWith
                    fadeOut() + scaleOut()
        }
    ) { state ->
        val iconId = if (state) selectedIconId else unselectedIconId
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
        )
    }

}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigationDisplay(navEntries: List<NavEntry<NavKey>>, onBack: () -> Unit = {}) {
    val bottomSheetSceneStrategy = remember { BottomSheetSceneStrategy<NavKey>() }
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
    )
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavKey>() }

    NavDisplay(
        sceneStrategy = bottomSheetSceneStrategy
            .then(listDetailSceneStrategy)
            .then(dialogSceneStrategy),
        entries = navEntries,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        onBack = onBack
    )
}

suspend fun showSnackbar(message: AppMessage, resources: Resources, snackbarHostState: SnackbarHostState) {
    when (message) {
        is AppMessage.NoConnection ->
            resources.getString(R.string.text_no_connection)
        else -> null
    }?.let {
        snackbarHostState.showSnackbar(it)
    }

}

@Composable
@Preview
fun AppScreenPreview() {
    DomainTrackTheme {
        AppNavigationScaffold(alertCount = 10, snackbarHostState = remember { SnackbarHostState() })
    }
}

@Composable
@Preview(name = "Tablet Preview", device = "id:Nexus 10")
fun AppScreenTabletPreview() {
    DomainTrackTheme {
        AppNavigationScaffold(alertCount = 5, snackbarHostState = remember { SnackbarHostState() })
    }
}