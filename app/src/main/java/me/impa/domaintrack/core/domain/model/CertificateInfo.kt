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

package me.impa.domaintrack.core.domain.model

data class CertificateInfo(
    val id: Long,
    val domainId: Long,
    val subdomain: String,
    val issuer: String,
    val country: String,
    val notBefore: Long,
    val notAfter: Long,
    val checkTime: Long,
    val updateState: UpdateState,
    val noCert: Boolean
) {
    companion object {
        val EMPTY = CertificateInfo(
            id = 0,
            domainId = 0,
            subdomain = "",
            issuer = "",
            country = "",
            notBefore = 0,
            notAfter = 0,
            checkTime = 0,
            updateState = UpdateState.UPDATE_QUEUED,
            noCert = true
        )
    }
}
