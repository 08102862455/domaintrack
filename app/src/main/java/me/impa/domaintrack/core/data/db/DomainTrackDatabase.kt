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

package me.impa.domaintrack.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import me.impa.domaintrack.core.data.db.dao.CertificateDao
import me.impa.domaintrack.core.data.db.dao.DomainDao
import me.impa.domaintrack.core.data.db.dao.RdapBootstrapDao
import me.impa.domaintrack.core.data.db.entity.CertificateEntity
import me.impa.domaintrack.core.data.db.entity.DomainEntity
import me.impa.domaintrack.core.data.db.entity.RdapBootstrapEntity

@Database(
    entities = [DomainEntity::class, CertificateEntity::class, RdapBootstrapEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class DomainTrackDatabase : RoomDatabase() {

    abstract fun domainDao(): DomainDao

    abstract fun certificateDao(): CertificateDao

    abstract fun rdapBootstrapDao(): RdapBootstrapDao

    companion object {
        const val DATABASE_NAME = "domain_track_db"
    }
}

