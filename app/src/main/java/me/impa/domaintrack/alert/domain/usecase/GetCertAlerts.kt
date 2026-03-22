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

package me.impa.domaintrack.alert.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import me.impa.domaintrack.alert.domain.model.AlertEntity
import me.impa.domaintrack.core.domain.DomainTrackRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

@Singleton
class GetCertAlerts @Inject constructor(
    private val repository: DomainTrackRepository
) {
    private val alertSettings = repository.settingsManager.settingsFlow.map { it.certAlert }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(date: Long) =
        alertSettings.flatMapConcat { settings ->
            repository.getExpiringCerts(date + settings.yellowLevel.days.inWholeMilliseconds)
                .map { certs -> certs.map { AlertEntity.create(it.cert, it.domain.domain, settings) } }
        }
}