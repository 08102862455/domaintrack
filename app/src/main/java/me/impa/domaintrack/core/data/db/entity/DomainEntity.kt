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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import me.impa.domaintrack.core.domain.model.DomainCertInfo
import me.impa.domaintrack.core.domain.model.DomainInfo
import me.impa.domaintrack.core.domain.model.UpdateState

@Entity(
    tableName = "domains",
    indices = [Index(value = ["domain"], unique = true)]
)
data class DomainEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "domain")
    val domain: String,
    @ColumnInfo(name = "creation_date")
    val creationDate: Long,
    @ColumnInfo(name = "expiration_date")
    val expirationDate: Long,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long,
    @ColumnInfo(name = "registrar")
    val registrar: String,
    @ColumnInfo(name = "notes")
    val notes: String,
    @ColumnInfo(name = "check_time")
    val checkTime: Long,
    @ColumnInfo(name = "update_state", defaultValue = "UPDATED")
    val updateState: UpdateState,
    @ColumnInfo(name = "no_info", defaultValue = "0")
    val noInfo: Boolean
)

fun DomainInfo.toEntity() = DomainEntity(
    domain = domain,
    creationDate = creationDate,
    expirationDate = expirationDate,
    updatedDate = updatedDate,
    registrar = registrar,
    notes = notes,
    checkTime = checkTime,
    updateState = updateState,
    noInfo = noInfo
)

fun DomainEntity.toDomain() = DomainInfo(
    id = id,
    domain = domain,
    creationDate = creationDate,
    expirationDate = expirationDate,
    updatedDate = updatedDate,
    registrar = registrar,
    notes = notes,
    checkTime = checkTime,
    updateState = updateState,
    noInfo = noInfo,
)