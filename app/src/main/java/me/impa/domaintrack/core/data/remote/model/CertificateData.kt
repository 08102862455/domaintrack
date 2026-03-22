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

package me.impa.domaintrack.core.data.remote.model

import me.impa.domaintrack.core.data.db.entity.CertificateEntity
import me.impa.domaintrack.core.domain.model.UpdateState

data class CertificateData(
    val id: Int,
    val domain: String,
    val subdomain: String,
    val issuer: String,
    val country: String,
    val notBefore: Long,
    val notAfter: Long
)

fun CertificateData.toEntity() =
    CertificateEntity(
        id = 0,
        domainId = 0,
        subdomain = subdomain,
        issuer = issuer,
        country = country,
        notBefore = notBefore,
        notAfter = notAfter,
        updateState = UpdateState.UPDATED,
        checkTime = System.currentTimeMillis(),
        noCert = false
    )