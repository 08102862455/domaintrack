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

package me.impa.domaintrack.core.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.impa.domaintrack.core.data.db.DomainTrackDatabase
import me.impa.domaintrack.core.data.db.dao.CertificateDao
import me.impa.domaintrack.core.data.db.dao.DomainDao
import me.impa.domaintrack.core.data.db.entity.toDomain
import me.impa.domaintrack.core.data.db.entity.toEntity
import me.impa.domaintrack.core.data.remote.DomainInfoService
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.domain.InternalDataStore
import me.impa.domaintrack.core.domain.SettingsManager
import me.impa.domaintrack.core.domain.model.CertDomainInfo
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.DomainCertInfo
import me.impa.domaintrack.core.domain.model.DomainInfo
import me.impa.domaintrack.core.domain.model.UpdateState
import javax.inject.Inject

class DomainTrackRepositoryImpl @Inject constructor(
    private val db: DomainTrackDatabase,
    private val domainInfoService: DomainInfoService,
    override val settingsManager: SettingsManager,
    private val internalDataStore: InternalDataStore
) : DomainTrackRepository {

    private val domainDao: DomainDao = db.domainDao()
    private val certificateDao: CertificateDao = db.certificateDao()

    override suspend fun getDomain(id: Long): DomainCertInfo? =
        domainDao.getDomainWithCerts(id)?.toDomain()

    override fun getDomainFlow(id: Long): Flow<DomainCertInfo?> =
        domainDao.getDomainWithCertsFlow(id = id).map { it?.toDomain() }

    override fun getDomainsFlow(): Flow<List<DomainCertInfo>> =
        domainDao.getDomainsWithCertsFlow()
            .map { domainWithCertificates -> domainWithCertificates.map { it.toDomain() } }

    override suspend fun getDomains(): List<DomainCertInfo> =
        domainDao.getDomainsWithCerts().map { it.toDomain() }

    override fun getStaleDomains(): Flow<List<DomainCertInfo>> =
        domainDao.getStaleDomains()
            .map { domainWithCertificates -> domainWithCertificates.map { it.toDomain() } }

    override fun getExpiringDomains(date: Long): Flow<List<DomainInfo>> =
        domainDao.getExpiringDomains(date).map { domains -> domains.map { it.toDomain() } }

    override fun getExpiringCerts(date: Long): Flow<List<CertDomainInfo>> =
        certificateDao.getExpiringCerts(date).map { certificates -> certificates.map { it.toDomain() } }

    override fun getCertsToActualize(): Flow<List<CertificateInfo>> =
        certificateDao.getUpdatingCerts()
            .map { certificates -> certificates.map { it.toDomain() } }

    override suspend fun saveDomain(domainInfo: DomainCertInfo): Long {
        return db.withTransaction {
            val domainId = if (domainInfo.domain.id == 0L) domainDao.insertDomain(domainInfo.domain.toEntity())
            else domainInfo.domain.id.also { domainDao.updateDomain(domainInfo.domain.toEntity()) }
            certificateDao.insertCertificates(domainInfo.certificates.map { it.copy(domainId = domainId).toEntity() })
            domainId
        }
    }

    override suspend fun deleteDomain(id: Long) {
        db.withTransaction {
            certificateDao.deleteByDomainId(id)
            domainDao.deleteDomain(id)
        }
    }

    override suspend fun renameDomain(id: Long, newDomain: String) {
        db.withTransaction {
            domainDao.renameDomain(id, newDomain)
            certificateDao.refreshCertsByDomainId(domainId = id)
        }
    }

    override suspend fun refreshDomain(id: Long) {
        domainDao.setDomainUpdateState(id, UpdateState.UPDATING)
        domainInfoService.refreshDomainInfo(id)
        domainDao.setDomainUpdateState(id, UpdateState.UPDATED)
    }

    override suspend fun queueDomainUpdate(id: Long) {
        domainDao.setDomainUpdateState(id, UpdateState.UPDATE_QUEUED)
        certificateDao.setCertsUpdateStateByDomain(id, UpdateState.UPDATE_QUEUED)
    }

    override suspend fun refreshCert(id: Long) {
        certificateDao.setCertificateUpdateState(id, UpdateState.UPDATING)
        domainInfoService.refreshCertInfo(id)
        certificateDao.setCertificateUpdateState(id, UpdateState.UPDATED)
    }

    override suspend fun queueDomainsUpdate() {
        domainDao.setDomainsUpdateState(UpdateState.UPDATE_QUEUED)
        certificateDao.setCertsUpdateState(UpdateState.UPDATE_QUEUED)
        internalDataStore.setLastFullUpdateTime(System.currentTimeMillis())
    }

}