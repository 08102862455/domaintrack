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

data class DomainInfo(
    val id: Long,
    val domain: String,
    val creationDate: Long,
    val expirationDate: Long,
    val updatedDate: Long,
    val registrar: String,
    val notes: String,
    val checkTime: Long,
    val updateState: UpdateState,
    val noInfo: Boolean,

    ) {
    companion object {
        val EMPTY = DomainInfo(
            id = 0,
            domain = "",
            creationDate = 0,
            expirationDate = 0,
            updatedDate = 0,
            registrar = "",
            notes = "",
            checkTime = 0,
            updateState = UpdateState.UPDATE_QUEUED,
            noInfo = false
        )
    }
}
