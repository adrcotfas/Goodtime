/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerManager.Companion.WIGGLE_ROOM_MILLIS
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.bl.isBreak
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun FinishedSessionContent(
    timerUiState: TimerUiState,
    historyUiState: HistoryUiState,
    onClose: (Boolean) -> Unit,
    onNext: (Boolean) -> Unit,
) {
    val timeProvider = koinInject<TimeProvider>()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    var elapsedRealtime by remember(lifecycleState) { mutableLongStateOf(timeProvider.elapsedRealtime()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes)
            elapsedRealtime = timeProvider.elapsedRealtime()
        }
    }
    FinishedSessionContent(timerUiState, historyUiState, elapsedRealtime, onClose, onNext)
}

@Composable
fun FinishedSessionContent(
    timerUiState: TimerUiState,
    historyUiState: HistoryUiState,
    elapsedRealtime: Long,
    onClose: (Boolean) -> Unit,
    onNext: (Boolean) -> Unit,
) {
    var addIdleMinutes by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isBreak = timerUiState.timerType.isBreak
        Text(
            text = if (isBreak) "Break finished" else "Work finished",
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        CurrentSessionCard(
            timerUiState,
            elapsedRealtime,
            addIdleMinutes,
        ) { addIdleMinutes = !addIdleMinutes }
        Spacer(modifier = Modifier.height(16.dp))
        HistoryCard(
            historyUiState,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Buttons({ onClose(addIdleMinutes) }, { onNext(addIdleMinutes) }, isBreak)
    }
}

@Composable
private fun CurrentSessionCard(
    timerUiState: TimerUiState,
    elapsedRealtime: Long,
    addIdleMinutes: Boolean,
    onAddIdleMinutesChanged: (Boolean) -> Unit,
) {
    val isBreak = timerUiState.isBreak
    val idleMinutes =
        (elapsedRealtime - timerUiState.endTime + WIGGLE_ROOM_MILLIS).milliseconds.inWholeMinutes

    Card(modifier = Modifier.wrapContentSize()) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(16.dp),
        ) {
            Text(
                "This session",
                style = MaterialTheme.typography.titleSmall.copy(MaterialTheme.colorScheme.primary),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.wrapContentHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        if (isBreak) "Break" else "Work",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        "${timerUiState.completedMinutes} min",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    )
                }

                if (!isBreak) {
                    val interruptions = timerUiState.timeSpentPaused.milliseconds.inWholeMinutes
                    if (interruptions > 0) {
                        Column(
                            modifier = Modifier.wrapContentHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text("Interruptions", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "$interruptions min",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            )
                        }
                    }

                    if (idleMinutes > 0) {
                        Column(
                            modifier = Modifier.wrapContentHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text("Idle", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "$idleMinutes min",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            )
                        }
                    }
                }
            }
            if (!isBreak && idleMinutes > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = addIdleMinutes,
                        onCheckedChange = onAddIdleMinutesChanged,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Consider idle time as extra work",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCard(historyUiState: HistoryUiState) {
    if (historyUiState.todayWorkMinutes > 0 || historyUiState.todayBreakMinutes > 0) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(),
            ) {
                Text(
                    "Today",
                    style = MaterialTheme.typography.titleSmall.copy(MaterialTheme.colorScheme.primary),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.wrapContentHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            "Work",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            "${historyUiState.todayWorkMinutes} min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                    Column(
                        modifier = Modifier.wrapContentHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            "Break",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            "${historyUiState.todayBreakMinutes} min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                    if (historyUiState.todayInterruptedMinutes > 0) {
                        Column(
                            modifier = Modifier.wrapContentHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                "Interruptions",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text(
                                "${historyUiState.todayInterruptedMinutes} min",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Buttons(
    onClose: () -> Unit,
    onNext: () -> Unit,
    isBreak: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            modifier = Modifier.weight(0.5f),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.12f,
                ),
            ),
            onClick = onClose,
        ) {
            Text("Close")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            modifier = Modifier.weight(0.5f),
            onClick = onNext,
        ) {
            Text(text = if (isBreak) "Start work" else "Start break")
        }
    }
}

@Preview
@Composable
fun FinishedSessionContentPreview() {
    FinishedSessionContent(
        timerUiState = TimerUiState(
            timerType = TimerType.WORK,
            completedMinutes = 25,
            timeSpentPaused = 3.minutes.inWholeMilliseconds,
        ),
        historyUiState = HistoryUiState(
            todayWorkMinutes = 90,
            todayBreakMinutes = 55,
            todayInterruptedMinutes = 3,
        ),
        elapsedRealtime = 3.minutes.inWholeMilliseconds,
        onClose = {},
        onNext = {},
    )
}
