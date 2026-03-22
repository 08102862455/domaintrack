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

package me.impa.domaintrack.alert.domain.model

import androidx.compose.runtime.Stable
import me.impa.domaintrack.core.domain.model.AlertSettings
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.DomainInfo
import me.impa.domaintrack.core.presentation.state.AlertLevel
import me.impa.domaintrack.core.util.calcDaysLeft
import me.impa.domaintrack.core.util.getFullDomain
import me.impa.domaintrack.core.util.msToDateString
import java.text.DateFormat

@Stable
sealed interface AlertEntity {
    val subject: String
    val expiration: String
    val daysLeft: Int
    val level: AlertLevel
    val domainId: Long
    val noInfo: Boolean

    @Stable
    data class Certificate(
        val certificate: CertificateInfo,
        val domain: String,
        override val subject: String = getFullDomain(domain, certificate.subdomain),
        override val expiration: String = msToDateString(certificate.notAfter, DateFormat.MEDIUM),
        override val daysLeft: Int = calcDaysLeft(certificate.notAfter),
        override val level: AlertLevel = AlertLevel.NONE,
        override val domainId: Long = certificate.domainId,
        override val noInfo: Boolean = certificate.noCert
    ) : AlertEntity

    @Stable
    data class Domain(
        val domain: DomainInfo,
        override val subject: String = domain.domain,
        override val expiration: String = msToDateString(domain.expirationDate, DateFormat.MEDIUM),
        override val daysLeft: Int = calcDaysLeft(domain.expirationDate),
        override val level: AlertLevel = AlertLevel.NONE,
        override val domainId: Long = domain.id,
        override val noInfo: Boolean = domain.noInfo
    ) : AlertEntity

    companion object {
        fun create(domain: DomainInfo, alertSettings: AlertSettings) =
            Domain(domain, level = AlertLevel.fromExpirationDate(domain.expirationDate, alertSettings))

        fun create(certificate: CertificateInfo, domain: String, alertSettings: AlertSettings) =
            Certificate(certificate, domain, level = AlertLevel.fromExpirationDate(certificate.notAfter, alertSettings))
    }
}