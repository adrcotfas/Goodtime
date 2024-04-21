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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatMilliseconds
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        val uiState = viewModel.uiState.collectAsState(MainUiState())
        TimerTextView(uiState.value.baseTime)
        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    viewModel.startTimer(TimerType.WORK)
                }
            ) {
                Text("start timer")
            }
            if (uiState.value.timerState != TimerState.RESET) {
                Button(
                    onClick = {
                        viewModel.toggleTimer()
                    }
                ) {
                    Text(if (uiState.value.timerState == TimerState.RUNNING) "pause" else "resume")
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
fun TimerTextView(seconds: Long) {
    Text(text = seconds.formatMilliseconds(), style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Black))
}
