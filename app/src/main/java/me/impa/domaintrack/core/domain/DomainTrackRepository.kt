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

package me.impa.domaintrack.core.domain

import kotlinx.coroutines.flow.Flow
import me.impa.domaintrack.core.domain.model.CertDomainInfo
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.DomainCertInfo
import me.impa.domaintrack.core.domain.model.DomainInfo

@Suppress("TooManyFunctions")
interface DomainTrackRepository {

    val settingsManager: SettingsManager

    suspend fun getDomain(id: Long): DomainCertInfo?

    fun getDomainFlow(id: Long): Flow<DomainCertInfo?>

    fun getDomainsFlow(): Flow<List<DomainCertInfo>>

    suspend fun getDomains(): List<DomainCertInfo>

    fun getStaleDomains(): Flow<List<DomainCertInfo>>

    fun getExpiringDomains(date: Long): Flow<List<DomainInfo>>

    fun getExpiringCerts(date: Long): Flow<List<CertDomainInfo>>

    suspend fun saveDomain(domainInfo: DomainCertInfo): Long

    suspend fun deleteDomain(id: Long)

    suspend fun renameDomain(id: Long, newDomain: String)

    suspend fun refreshDomain(id: Long)

    fun getCertsToActualize(): Flow<List<CertificateInfo>>

    suspend fun refreshCert(id: Long)

    suspend fun queueDomainsUpdate()
    suspend fun queueDomainUpdate(id: Long)

}