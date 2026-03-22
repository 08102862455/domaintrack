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

package me.impa.domaintrack.ui

import android.app.ComponentCaller
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import me.impa.domaintrack.alert.presentation.AlertManager
import me.impa.domaintrack.app.presentation.AppScreen
import me.impa.domaintrack.core.presentation.DomainObserver
import me.impa.domaintrack.core.presentation.navigation.Navigator
import me.impa.domaintrack.core.presentation.navigation.Route
import me.impa.domaintrack.worker.presentation.RefreshDomainWorker
import me.impa.domaintrack.worker.presentation.ScheduleObserver
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var domainObserver: DomainObserver

    @Inject
    lateinit var scheduleObserver: ScheduleObserver

    @Inject
    lateinit var alertManager: AlertManager

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            processIntent(intent)
        }

        lifecycle.apply {
            addObserver(domainObserver)
            addObserver(scheduleObserver)
            addObserver(alertManager)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
            WindowCompat.enableEdgeToEdge(window)
        setContent {
            AppScreen()
        }
    }

    private fun processIntent(intent: Intent) {
        val isFromNotification = intent.getBooleanExtra(RefreshDomainWorker.EXTRA_FROM_NOTIFICATION, false)
        if (isFromNotification) {
            navigator.navigate(Route.Alerts)
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        processIntent(intent)
    }
}

