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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import me.impa.domaintrack.alert.presentation.AlertsScreen
import me.impa.domaintrack.core.presentation.navigation.BottomSheetSceneStrategy
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.domaindelete.presentation.DomainDeleteScreen
import me.impa.domaintrack.domaindelete.presentation.DomainDeleteViewModel
import me.impa.domaintrack.domainedit.presentation.DomainEditScreen
import me.impa.domaintrack.domainedit.presentation.DomainEditViewModel
import me.impa.domaintrack.domainlist.presentation.DomainListScreen
import me.impa.domaintrack.domainview.presentation.DomainViewScreen
import me.impa.domaintrack.domainview.presentation.DomainViewViewModel
import me.impa.domaintrack.onboard.presentation.OnboardingScreen
import me.impa.domaintrack.schedule.presentation.ScheduleScreen
import me.impa.domaintrack.settings.presentation.AlertRulesScreen
import me.impa.domaintrack.settings.presentation.BatteryOptimizationScreen
import me.impa.domaintrack.settings.presentation.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
fun navigationEntryProvider() = entryProvider<NavKey> {
    entry<Route.DomainList>(metadata = ListDetailSceneStrategy.listPane()) { DomainListScreen() }
    entry<Route.Settings> { SettingsScreen() }
    entry<Route.Alerts> { AlertsScreen() }
    entry<Route.DomainDetails>(metadata = ListDetailSceneStrategy.detailPane()) { entry ->
        val viewModel = hiltViewModel<DomainViewViewModel, DomainViewViewModel.DomainViewViewModelFactory> {
            it.create(entry.domainId)
        }
        DomainViewScreen(viewModel)
    }
    entry<Route.AddDomain>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        val viewModel = hiltViewModel<DomainEditViewModel, DomainEditViewModel.DomainEditViewModelFactory> {
            it.create(0, "")
        }
        DomainEditScreen(viewModel)
    }
    entry<Route.EditDomain>(metadata = BottomSheetSceneStrategy.bottomSheet()) { entry ->
        val viewModel = hiltViewModel<DomainEditViewModel, DomainEditViewModel.DomainEditViewModelFactory> {
            it.create(entry.domainId, entry.domain)
        }
        DomainEditScreen(viewModel)
    }
    entry<Route.DeleteDomain>(metadata = DialogSceneStrategy.dialog()) { entry ->
        val viewModel = hiltViewModel<DomainDeleteViewModel, DomainDeleteViewModel.DomainDeleteViewModelFactory> {
            it.create(entry.domainId)
        }
        DomainDeleteScreen(viewModel)
    }
    entry<Route.EditSchedule>(metadata = DialogSceneStrategy.dialog()) { ScheduleScreen() }
    entry<Route.AlertRules> { AlertRulesScreen() }
    entry<Route.BatteryOptimization> { BatteryOptimizationScreen() }
    entry<Route.Onboarding> { OnboardingScreen() }


}