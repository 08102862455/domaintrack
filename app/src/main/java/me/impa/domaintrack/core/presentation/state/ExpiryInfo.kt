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
import me.impa.domaintrack.core.domain.model.DomainInfo
import me.impa.domaintrack.core.util.calcDaysLeft

data class ExpiryInfo(
    val daysLeft: Int,
    val alertLevel: AlertLevel,
    val expirationDate: Long,
) {
    companion object {
        fun create(domain: DomainInfo, alertSettings: AlertSettings) = ExpiryInfo(
            daysLeft = calcDaysLeft(domain.expirationDate),
            alertLevel = AlertLevel.fromExpirationDate(domain.expirationDate, alertSettings),
            expirationDate = domain.expirationDate
        )

        fun create(certificateInfo: CertificateInfo, alertSettings: AlertSettings) = ExpiryInfo(
            daysLeft = calcDaysLeft(certificateInfo.notAfter),
            alertLevel = AlertLevel.fromExpirationDate(certificateInfo.notAfter, alertSettings),
            expirationDate = certificateInfo.notAfter
        )
    }
}
