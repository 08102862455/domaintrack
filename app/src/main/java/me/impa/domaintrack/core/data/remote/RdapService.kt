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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.impa.domaintrack.core.data.db.dao.RdapBootstrapDao
import me.impa.domaintrack.core.data.db.entity.RdapBootstrapEntity
import me.impa.domaintrack.core.data.remote.api.IanaBootstrapApi
import me.impa.domaintrack.core.data.remote.api.RdapApi
import me.impa.domaintrack.core.data.remote.dto.fullName
import me.impa.domaintrack.core.data.remote.model.DomainData
import me.impa.domaintrack.core.di.IoDispatcher
import me.impa.domaintrack.core.domain.InternalDataStore
import me.impa.domaintrack.core.util.extractTld
import me.impa.domaintrack.core.util.isIpAddress
import me.impa.domaintrack.core.util.msToDateTimeString
import timber.log.Timber
import java.io.IOException
import java.net.IDN
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

private val CACHE_TTL_MS = 30.days.inWholeMilliseconds

@Singleton
class RdapService @Inject constructor(
    private val rdapDao: RdapBootstrapDao,
    private val internalDataStore: InternalDataStore,
    private val ianaBootstrapApi: IanaBootstrapApi,
    private val rdapApi: RdapApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private suspend fun refreshBootstrapCache() {
        val rawData = withContext(ioDispatcher) { ianaBootstrapApi.getBootstrapData() }
        if (!rawData.isSuccessful || rawData.body() == null)
            throw IOException("Failed to fetch RDAP bootstrap data")

        val rdapData = rawData.body() ?: throw IOException("Failed to parse bootstrap data")

        val data = rdapData.services.flatMap {
            if (it.size == 2) {
                it[0].map { tld -> RdapBootstrapEntity(tld, it[1]) }
            } else {
                emptyList()
            }
        }
        Timber.d("Bootstrap data: ${data.size} records loaded")
        rdapDao.insertRdapBootstrapList(data)
        internalDataStore.setIanaBootstrapDate(System.currentTimeMillis())
    }

    private suspend fun getRdapServers(domain: String): List<String> {
        require(!withContext(ioDispatcher) { isIpAddress(domain) }) {
            "Invalid domain name"
        }

        // Do we need to refresh cache?
        val time = System.currentTimeMillis()
        val ianaBootstrapDate = internalDataStore.getIanaBootstrapDate()
        if (time - ianaBootstrapDate.first() > CACHE_TTL_MS) {
            try {
                refreshBootstrapCache()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        val tld = extractTld(domain)

        return withContext(ioDispatcher) { rdapDao.getRdapBootstrap(tld) }?.url?.takeIf { it.isNotEmpty() }
            ?: throw IOException("No RDAP bootstrap data found")
    }

    @Suppress("ReturnCount")
    suspend fun query(domain: String): Result<DomainData> {
        val idnDomain = IDN.toASCII(domain, IDN.ALLOW_UNASSIGNED)

        val servers = try {
            getRdapServers(idnDomain)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        var error: Exception? = null

        servers.forEach { server ->
            try {
                val response = withContext(ioDispatcher) {
                    val url = "${server.removeSuffix("/")}/domain/$idnDomain"
                    Timber.d("RDAP request: $url")
                    rdapApi.getRdapDomainInfo(url)
                }
                response.takeIf { it.isSuccessful }?.body()?.run {
                    val creationDate = events?.firstOrNull { it.eventAction == "registration" }?.eventDate?.time ?: 0L
                    val expirationDate = events?.firstOrNull { it.eventAction == "expiration" }?.eventDate?.time ?: 0L
                    val updatedDate = events?.firstOrNull { it.eventAction == "last changed" }?.eventDate?.time ?: 0L
                    val registrar = entities?.firstOrNull { it.roles?.contains("registrar") == true }
                        ?.vcardArray?.fullName ?: ""

                    Timber.d("RDAP response: $creationDate, $expirationDate, $updatedDate, $registrar")
                    return Result.success(
                        DomainData(
                            domain = domain,
                            creationDate = creationDate,
                            expirationDate = expirationDate,
                            updatedDate = updatedDate,
                            registrar = registrar
                        )
                    )
                }
            } catch (e: IOException) {
                Timber.e(e)
                error = e
            }
        }
        return Result.failure(error ?: Exception("No response from RDAP servers"))
    }
}