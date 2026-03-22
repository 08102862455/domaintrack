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
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.UpdateState
import me.impa.domaintrack.core.util.getCountryFlagEmoji
import me.impa.domaintrack.core.util.getFullDomain
import me.impa.domaintrack.core.util.msToDateString
import me.impa.domaintrack.core.util.msToDateTimeString
import java.text.DateFormat

data class CertificateInfoState(
    val id: Long,
    val fullDomain: String,
    val subdomain: String,
    val issuer: String,
    val country: String,
    val notBeforeShort: String,
    val notBeforeMedium: String,
    val notAfterShort: String,
    val notAfterMedium: String,
    val checkTime: String,
    val checkTimeRaw: Long,
    val isUpdating: Boolean,
    val noCert: Boolean,
    val expiryInfo: ExpiryInfo
)

fun CertificateInfo.toState(domain: String, alertSettings: AlertSettings) = CertificateInfoState(
    id = id,
    fullDomain = getFullDomain(domain, subdomain),
    subdomain = subdomain,
    issuer = issuer.let {
        val flag = getCountryFlagEmoji(country)
        if (flag.isNotEmpty()) "$flag $it" else it
    },
    country = country,
    notBeforeShort = msToDateString(notBefore),
    notBeforeMedium = msToDateString(notBefore, DateFormat.MEDIUM),
    notAfterShort = msToDateString(notAfter),
    notAfterMedium = msToDateString(notAfter, DateFormat.MEDIUM),
    checkTime = msToDateTimeString(checkTime),
    checkTimeRaw = checkTime,
    isUpdating = updateState == UpdateState.UPDATING || checkTime == 0L,
    noCert = noCert,
    expiryInfo = ExpiryInfo.create(this, alertSettings)
)
