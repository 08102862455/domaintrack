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

package me.impa.domaintrack.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface InternalDataStore {

    suspend fun setIanaBootstrapDate(date: Long)
    fun getIanaBootstrapDate(): Flow<Long>

    suspend fun setListSortType(type: Int)
    fun getListSortType(): Flow<Int>

    suspend fun setBatteryOptimizationShown(shown: Boolean)
    fun getBatteryOptimizationShown(): Flow<Boolean>

    suspend fun setLastFullUpdateTime(time: Long)
    fun getLastFullUpdateTime(): Flow<Long>

    suspend fun setMonitorOnboardingShown(shown: Boolean)
    fun getMonitorOnboardingShown(): Flow<Boolean>

}