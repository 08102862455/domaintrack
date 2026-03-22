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

package me.impa.domaintrack.core.data.remote

import me.impa.domaintrack.core.data.db.dao.CertificateDao
import me.impa.domaintrack.core.data.db.dao.DomainDao
import me.impa.domaintrack.core.data.remote.model.toEntity
import me.impa.domaintrack.core.domain.model.UpdateState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainInfoService @Inject constructor(
    private val whoisService: WhoisService,
    private val rdapService: RdapService,
    private val sslCertService: SslCertService,
    private val domainDao: DomainDao,
    private val certDao: CertificateDao
) {

    suspend fun refreshDomainInfo(id: Long) {
        val domainData = domainDao.getDomainWithCerts(id) ?: return

        val domainInfo = rdapService.query(domainData.domain.domain).takeIf { it.isSuccess }
            ?: whoisService.query(domainData.domain.domain)

        domainInfo.getOrNull()?.let {
            domainDao.updateDomain(it.toEntity().copy(id = domainData.domain.id, noInfo = false))
        } ?: domainDao.updateDomain(
            domainData.domain.copy(
                noInfo = true,
                checkTime = System.currentTimeMillis(),
                updateState = UpdateState.UPDATED
            )
        ).also {
            Timber.e(domainInfo.exceptionOrNull())
        }
    }

    suspend fun refreshCertInfo(id: Long) {
        val certData = certDao.getCertificateWithDomain(id) ?: return

        val sslCertInfo = sslCertService.query(certData.domain.domain, certData.certificate.subdomain)

        sslCertInfo.getOrNull()?.let {
            certDao.updateCertificate(
                it.toEntity().copy(
                    id = certData.certificate.id,
                    domainId = certData.certificate.domainId,
                    noCert = false
                )
            )
        } ?: certDao.updateCertificate(
            certData.certificate.copy(
                noCert = true,
                checkTime = System.currentTimeMillis(),
                updateState = UpdateState.UPDATED
            ).also { Timber.e(sslCertInfo.exceptionOrNull()) }
        )
    }
}