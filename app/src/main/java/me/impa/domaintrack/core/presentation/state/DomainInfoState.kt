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

package me.impa.domaintrack.core.presentation.state

import me.impa.domaintrack.core.domain.model.AlertSettings
import me.impa.domaintrack.core.domain.model.DomainCertInfo
import me.impa.domaintrack.core.domain.model.UpdateState
import me.impa.domaintrack.core.util.msToDateString
import me.impa.domaintrack.core.util.msToDateTimeString
import java.text.DateFormat

data class DomainInfoState(
    val id: Long,
    val domain: String,
    val creationDateShort: String,
    val creationDateMedium: String,
    val expirationDateShort: String,
    val expirationDateMedium: String,
    val updatedDateShort: String,
    val updatedDateMedium: String,
    val registrar: String,
    val notes: String,
    val checkTime: String,
    val checkTimeRaw: Long,
    val isUpdating: Boolean,
    val noInfo: Boolean,
    val expiryInfo: ExpiryInfo,
    val certificates: List<CertificateInfoState>
)

fun DomainCertInfo.toState(domainAlertSettings: AlertSettings, certAlertSettings: AlertSettings) = DomainInfoState(
    id = domain.id,
    domain = domain.domain,
    creationDateShort = msToDateString(domain.creationDate),
    creationDateMedium = msToDateString(domain.creationDate, DateFormat.MEDIUM),
    expirationDateShort = msToDateString(domain.expirationDate),
    expirationDateMedium = msToDateString(domain.expirationDate, DateFormat.MEDIUM),
    updatedDateShort = msToDateString(domain.updatedDate),
    updatedDateMedium = msToDateString(domain.updatedDate, DateFormat.MEDIUM),
    registrar = domain.registrar,
    notes = domain.notes,
    checkTime = msToDateTimeString(domain.checkTime),
    checkTimeRaw = domain.checkTime,
    isUpdating = domain.updateState == UpdateState.UPDATING || domain.checkTime == 0L,
    noInfo = domain.noInfo,
    expiryInfo = ExpiryInfo.create(domain, domainAlertSettings),
    certificates = certificates.map { it.toState(domain.domain, certAlertSettings) }

)
