package com.apps.adrcotfas.goodtime.main

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatMilliseconds
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.settings.user_interface.InitTimerStyle
import com.apps.adrcotfas.goodtime.ui.timerTextStyles
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {

    InitTimerStyle(viewModel)

    val timerState = viewModel.timerState.collectAsStateWithLifecycle(TimerUiState())

    val timerStyle by viewModel.uiState.map { it.timerStyle }
        .collectAsStateWithLifecycle(TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE))

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (timerStyle.minSize != TimerStyleData.INVALID_MIN_SIZE) {
            TimerTextView(timerStyle, seconds = timerState.value.baseTime)
        }
        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    viewModel.startTimer(TimerType.WORK)
                }
            ) {
                Text("start timer")
            }
            if (timerState.value.timerState != TimerState.RESET) {
                Button(
                    onClick = {
                        viewModel.toggleTimer()
                    }
                ) {
                    Text(if (timerState.value.timerState == TimerState.RUNNING) "pause" else "resume")
                }
                //TODO: testing purposes / remove this
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

@Composable
fun TimerTextView(timerStyle: TimerStyleData, seconds: Long) {
    Text(
        text = seconds.formatMilliseconds(),
        style = TextStyle(
            fontSize = timerStyle.fontSize.em,
            fontFamily = timerTextStyles[timerStyle.fontIndex]!![timerStyle.fontWeight]
        ),
    )
}
