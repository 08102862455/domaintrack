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

package me.impa.domaintrack.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.impa.domaintrack.core.data.db.entity.CertificateEntity
import me.impa.domaintrack.core.data.db.entity.CertificateWithDomainEntity
import me.impa.domaintrack.core.domain.model.UpdateState

@Dao
interface CertificateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificates(certificates: List<CertificateEntity>)

    @Update
    suspend fun updateCertificate(certificate: CertificateEntity)

    @Query("UPDATE certificates SET update_state = 'UPDATE_QUEUED' WHERE domain_id = :domainId")
    suspend fun refreshCertsByDomainId(domainId: Long)

    @Query("SELECT * FROM certificates")
    fun getCertificates(): Flow<List<CertificateEntity>>

    @Transaction
    @Query("SELECT * from certificates WHERE not_after < :date AND update_state = 'UPDATED' AND no_cert = 0")
    fun getExpiringCerts(date: Long): Flow<List<CertificateWithDomainEntity>>

    @Query("DELETE FROM certificates WHERE domain_id = :domainId")
    suspend fun deleteByDomainId(domainId: Long)

    @Query("SELECT * FROM certificates WHERE update_state = 'UPDATE_QUEUED'")
    fun getUpdatingCerts(): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE id = :id")
    suspend fun getCertificate(id: Long): CertificateEntity?

    @Transaction
    @Query("SELECT * FROM certificates WHERE id = :id")
    suspend fun getCertificateWithDomain(id: Long): CertificateWithDomainEntity?

    @Query("UPDATE certificates SET update_state = :state WHERE id = :id")
    suspend fun setCertificateUpdateState(id: Long, state: UpdateState)

    @Query("UPDATE certificates SET update_state = :updateState")
    suspend fun setCertsUpdateState(updateState: UpdateState)

    @Query("UPDATE certificates SET update_state = :updateState WHERE domain_id = :domainId")
    suspend fun setCertsUpdateStateByDomain(domainId: Long, updateState: UpdateState)
}