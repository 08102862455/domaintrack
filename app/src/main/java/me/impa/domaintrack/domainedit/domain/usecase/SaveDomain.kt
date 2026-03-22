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

package me.impa.domaintrack.domainedit.domain.usecase

import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.DomainCertInfo
import javax.inject.Inject

class SaveDomain @Inject constructor(
    private val repository: DomainTrackRepository
) {
    suspend operator fun invoke(domainId: Long, domainName: String): Long {
        val resultDomain = domainName.trim().lowercase()

        require(resultDomain.isNotEmpty(), { "Domain name cannot be empty" })

        return if (domainId == 0L) {
            repository.saveDomain(
                DomainCertInfo.EMPTY.copy(
                    domain = DomainCertInfo.EMPTY.domain.copy(
                        id = domainId,
                        domain = resultDomain,
                    ),
                    certificates = listOf(
                        CertificateInfo.EMPTY
                    )
                )
            )
        } else {
            repository.renameDomain(domainId, resultDomain)
            domainId
        }
    }
}