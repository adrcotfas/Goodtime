package com.apps.adrcotfas.goodtime.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.settings.user_interface.InitTimerStyle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {

    InitTimerStyle(viewModel)

    val timerUiState by viewModel.timerUiState.collectAsStateWithLifecycle(TimerUiState())
    val timerStyle by viewModel.uiState.map { it.timerStyle }
        .collectAsStateWithLifecycle(TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE))
    val label by viewModel.uiState.map { it.label }.filterNotNull()
        .collectAsStateWithLifecycle(Label.defaultLabel())

    //TODO:
    // dialcontrol should be accessible only by dragging from the timer, not anywhere on the screen
    //TODO: add tooltips to the selected dial like in the original app

    //TODO: haptic feedback to the timer pause/resume and other actions

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MainTimerView(
                timerUiState = timerUiState,
                timerStyle = timerStyle,
                label = label,
                onStart = viewModel::startTimer,
                onToggle = viewModel::toggleTimer
            )

            Spacer(modifier = Modifier.height(64.dp))
            if (true) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            viewModel.startTimer(TimerType.WORK)
                        }
                    ) {
                        Text("start timer")
                    }
                    if (timerUiState.timerState != TimerState.RESET) {
                        Button(
                            onClick = {
                                viewModel.next()
                            }
                        ) {
                            Text("next")
                        }
                        Button(
                            onClick = {
                                viewModel.finishTimer()
                            }
                        ) {
                            Text("finish")
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.resetTimer()
                        }
                    ) {
                        Text("stop")
                    }
                }
            }
        }

    }

}
