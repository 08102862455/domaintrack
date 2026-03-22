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

package me.impa.domaintrack.core.presentation.navigation

import android.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import me.impa.domaintrack.ui.theme.DomainTrackTheme

@Composable
fun BasicDialogScaffold(
    title: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = Modifier
        .clip(AlertDialogDefaults.shape)
        .background(AlertDialogDefaults.containerColor)
        ) {
        Column(modifier = Modifier.padding(24.dp)) {
            title?.let { title ->
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    val mergedStyle = LocalTextStyle.current.merge(MaterialTheme.typography.headlineSmall)
                    CompositionLocalProvider(
                        LocalContentColor provides AlertDialogDefaults.titleContentColor,
                        LocalTextStyle provides mergedStyle
                    ) {
                        title()
                    }
                }
            }
            CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.textContentColor) {
                content()
            }
            actions?.let { actions ->
                Box(modifier = Modifier.padding(top = 24.dp)) {
                    actions()
                }
            }

        }
    }
}

@Preview
@Composable
private fun BasicDialogScaffoldPreview() {
    DomainTrackTheme {
        BasicDialogScaffold(
            title = { Text(text = "Dialog Title") },
            content = { Text(text = "This is the content of the dialog.") },
            actions = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { }) {
                        Text(text = "Action 1")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { }) {
                        Text(text = "Action 2")
                    }
                }
            }
        )
    }
}
