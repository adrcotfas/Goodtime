package com.apps.adrcotfas.goodtime.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.apps.adrcotfas.goodtime.ui.localColorsPalette
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {

    InitTimerStyle(viewModel)

    val timerState by viewModel.timerState.collectAsStateWithLifecycle(TimerUiState())

    val timerStyle by viewModel.uiState.map { it.timerStyle }
        .collectAsStateWithLifecycle(TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE))

    val label by viewModel.uiState.map { it.label }
        .collectAsStateWithLifecycle(Label.defaultLabel())
    val labelColorIndex = label?.colorIndex ?: Label.DEFAULT_LABEL_COLOR_INDEX
    val labelColor = MaterialTheme.localColorsPalette.colors[labelColorIndex.toInt()]

    val isCountdown = timerState.isCountdown
    val isBreak = timerState.timerType != TimerType.WORK

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (timerStyle.minSize != TimerStyleData.INVALID_MIN_SIZE) {
            label?.let {
                CurrentStatusAndLabelSection(
                    color = labelColor,
                    labelName = it.name,
                    isBreak = isBreak,
                    isActive = timerState.isActive(),
                    isPaused = timerState.isPaused(),
                    streak = timerState.longBreakData.streak,
                    sessionsBeforeLongBreak = timerState.sessionsBeforeLongBreak,
                    showStatus = timerStyle.showStatus,
                    showStreak = timerStyle.showStreak,
                    showLabel = timerStyle.showLabel
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TimerTextView(
                isPaused = timerState.isPaused(),
                timerStyle = timerStyle,
                millis = timerState.baseTime,
                color = labelColor,
                onPress = { if (!timerState.isActive()) viewModel.startTimer(TimerType.WORK) else viewModel.toggleTimer() })
        }
        if (isCountdown) {

        }

        Spacer(modifier = Modifier.height(32.dp))
        if (false) {

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        viewModel.startTimer(TimerType.WORK)
                    }
                ) {
                    Text("start timer")
                }
                if (timerState.timerState != TimerState.RESET) {
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
