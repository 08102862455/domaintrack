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
import me.impa.domaintrack.core.data.db.entity.DomainEntity
import me.impa.domaintrack.core.data.db.entity.DomainWithCertsEntity
import me.impa.domaintrack.core.domain.model.UpdateState

@Dao
@Suppress("TooManyFunctions")
interface DomainDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDomain(domain: DomainEntity): Long

    @Update
    suspend fun updateDomain(domain: DomainEntity)

    @Query("DELETE FROM domains WHERE id = :id")
    suspend fun deleteDomain(id: Long)

    @Transaction
    @Query("SELECT * FROM domains")
    fun getDomainsWithCertsFlow(): Flow<List<DomainWithCertsEntity>>

    @Transaction
    @Query("SELECT * FROM domains")
    suspend fun getDomainsWithCerts(): List<DomainWithCertsEntity>

    @Transaction
    @Query("SELECT * FROM domains WHERE update_state = 'UPDATE_QUEUED'")
    fun getStaleDomains(): Flow<List<DomainWithCertsEntity>>

    @Query("SELECT * FROM domains WHERE expiration_date < :date AND expiration_date > 0 " +
            "AND update_state = 'UPDATED' AND no_info = 0")
    fun getExpiringDomains(date: Long): Flow<List<DomainEntity>>

    @Transaction
    @Query("SELECT * FROM domains WHERE id = :id")
    suspend fun getDomainWithCerts(id: Long): DomainWithCertsEntity?

    @Transaction
    @Query("SELECT * FROM domains WHERE id = :id")
    fun getDomainWithCertsFlow(id: Long): Flow<DomainWithCertsEntity?>

    @Query("UPDATE domains SET domain = :newDomain, update_state = 'UPDATE_QUEUED' WHERE id = :id")
    suspend fun renameDomain(id: Long, newDomain: String)

    @Query("UPDATE domains SET update_state = :state WHERE id = :id")
    suspend fun setDomainUpdateState(id: Long, state: UpdateState)

    @Query("UPDATE domains SET update_state = :updateState")
    fun setDomainsUpdateState(updateState: UpdateState)

}