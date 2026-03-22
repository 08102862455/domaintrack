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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import me.impa.domaintrack.core.domain.model.CertificateInfo
import me.impa.domaintrack.core.domain.model.UpdateState

@Entity(
    tableName = "certificates",
    foreignKeys = [
        ForeignKey(
            entity = DomainEntity::class,
            parentColumns = ["id"],
            childColumns = ["domain_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CertificateEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "domain_id", index = true)
    val domainId: Long,
    @ColumnInfo(name = "subdomain")
    val subdomain: String,
    @ColumnInfo(name = "issuer")
    val issuer: String,
    @ColumnInfo(name = "country")
    val country: String,
    @ColumnInfo(name = "not_before")
    val notBefore: Long,
    @ColumnInfo(name = "not_after")
    val notAfter: Long,
    @ColumnInfo(name = "check_time")
    val checkTime: Long,
    @ColumnInfo(name = "update_state", defaultValue = "UPDATED")
    val updateState: UpdateState,
    @ColumnInfo(name = "no_cert", defaultValue = "1")
    val noCert: Boolean
)

fun CertificateEntity.toDomain() = CertificateInfo(
    id = id,
    domainId = domainId,
    subdomain = subdomain,
    issuer = issuer,
    country = country,
    notBefore = notBefore,
    notAfter = notAfter,
    checkTime = checkTime,
    updateState = updateState,
    noCert = noCert
)

fun CertificateInfo.toEntity() = CertificateEntity(
    id = id,
    subdomain = subdomain,
    domainId = domainId,
    issuer = issuer,
    country = country,
    notBefore = notBefore,
    notAfter = notAfter,
    checkTime = checkTime,
    updateState = updateState,
    noCert = noCert
)


