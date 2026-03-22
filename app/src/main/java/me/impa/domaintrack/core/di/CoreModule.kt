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

package me.impa.domaintrack.core.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.impa.domaintrack.core.data.DomainTrackRepositoryImpl
import me.impa.domaintrack.core.data.InternalDataStoreImpl
import me.impa.domaintrack.core.data.SettingsManagerImpl
import me.impa.domaintrack.core.data.db.DomainTrackDatabase
import me.impa.domaintrack.core.domain.DomainTrackRepository
import me.impa.domaintrack.core.domain.InternalDataStore
import me.impa.domaintrack.core.domain.SettingsManager
import me.impa.domaintrack.core.presentation.navigation.NavigationState

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    @Singleton
    @Binds
    abstract fun bindDomainTrackRepository(impl: DomainTrackRepositoryImpl): DomainTrackRepository

    @Singleton
    @Binds
    abstract fun bindInternalDataStore(impl: InternalDataStoreImpl): InternalDataStore

    @Singleton
    @Binds
    abstract fun bindSettingsManager(impl: SettingsManagerImpl): SettingsManager

    companion object {

        @Singleton
        @Provides
        fun provideDatabase(@ApplicationContext appContext: Context): DomainTrackDatabase =
            Room.databaseBuilder<DomainTrackDatabase>(appContext, DomainTrackDatabase.DATABASE_NAME)
                .addMigrations()
                .build()

        @Provides
        fun provideDomainDao(database: DomainTrackDatabase) = database.domainDao()

        @Provides
        fun provideCertificateDao(database: DomainTrackDatabase) = database.certificateDao()

        @Provides
        fun provideRdapBootstrapDao(database: DomainTrackDatabase) = database.rdapBootstrapDao()

        @Provides
        @Singleton
        fun provideNavigationState() = NavigationState()

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @DefaultDispatcher
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Provides
        @MainDispatcher
        fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    }
}