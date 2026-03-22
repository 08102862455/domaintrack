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

package me.impa.domaintrack.core.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import me.impa.domaintrack.core.presentation.state.AppMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppMessageBus @Inject constructor() {

    private val messageChannel = Channel<AppMessage>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val messages = messageChannel.receiveAsFlow()

    fun sendMessage(message: AppMessage) {
        messageChannel.trySend(message)
    }


}