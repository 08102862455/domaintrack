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

package me.impa.domaintrack.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.impa.domaintrack.core.domain.InternalDataStore
import javax.inject.Inject

class InternalDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : InternalDataStore {

    override fun getIanaBootstrapDate(): Flow<Long> = context.dataStore.data.map {
            it[ianaBootstrapDateKey] ?: 0L
        }.distinctUntilChanged()

    override suspend fun setIanaBootstrapDate(date: Long) {
        context.dataStore.edit {
            it[ianaBootstrapDateKey] = date
        }
    }

    override suspend fun setListSortType(type: Int) {
        context.dataStore.edit {
            it[listSortTypeKey] = type
        }
    }

    override fun getListSortType(): Flow<Int> = context.dataStore.data.map {
        it[listSortTypeKey] ?: 0
    }.distinctUntilChanged()

    override suspend fun setBatteryOptimizationShown(shown: Boolean) {
        context.dataStore.edit {
            it[batteryOptimizationShownKey] = shown
        }
    }

    override fun getBatteryOptimizationShown(): Flow<Boolean> = context.dataStore.data.map {
        it[batteryOptimizationShownKey] ?: false
    }.distinctUntilChanged()

    override suspend fun setLastFullUpdateTime(time: Long) {
        context.dataStore.edit {
            it[lastFullUpdateTimeKey] = time
        }
    }

    override fun getLastFullUpdateTime(): Flow<Long> = context.dataStore.data.map {
        it[lastFullUpdateTimeKey] ?: 0L
    }.distinctUntilChanged()

    override suspend fun setMonitorOnboardingShown(shown: Boolean) {
        context.dataStore.edit {
            it[monitorOnboardingShownKey] = shown
        }
    }

    override fun getMonitorOnboardingShown(): Flow<Boolean> = context.dataStore.data.map {
        it[monitorOnboardingShownKey] ?: false
    }.distinctUntilChanged()

    companion object {
        private val Context.dataStore by preferencesDataStore("internal_settings")

        private val ianaBootstrapDateKey = longPreferencesKey("iana_bootstrap_date")

        private val listSortTypeKey = intPreferencesKey("list_sort_type")

        private val batteryOptimizationShownKey = booleanPreferencesKey("battery_optimization_shown")

        private val lastFullUpdateTimeKey = longPreferencesKey("last_full_update_time")

        private val monitorOnboardingShownKey = booleanPreferencesKey("monitor_onboarding_shown")


    }

}