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

package me.impa.domaintrack.core.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import me.impa.domaintrack.core.domain.model.CertDomainInfo

data class CertificateWithDomainEntity (
    @Embedded val certificate: CertificateEntity,
    @Relation(
        parentColumn = "domain_id",
        entityColumn = "id"
    )
    val domain: DomainEntity
)

fun CertificateWithDomainEntity.toDomain() = CertDomainInfo(
    cert = certificate.toDomain(),
    domain = domain.toDomain()

)