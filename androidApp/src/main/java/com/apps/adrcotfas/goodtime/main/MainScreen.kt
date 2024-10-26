package com.apps.adrcotfas.goodtime.main

import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.main.dial_control.DialConfig
import com.apps.adrcotfas.goodtime.main.dial_control.DialControl
import com.apps.adrcotfas.goodtime.main.dial_control.DialControlButton
import com.apps.adrcotfas.goodtime.main.dial_control.DialRegion
import com.apps.adrcotfas.goodtime.main.dial_control.rememberDialControlState
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

    val state = rememberDialControlState(
        options = DialRegion.entries,
        enabledOptions = DialRegion.entries.minus(DialRegion.LEFT),
        config = DialConfig(size = timerStyle.currentScreenWidth.dp - 64.dp),
        onSelected = {
            when (it) {
                DialRegion.TOP -> {
                    viewModel.addOneMinute()
                }

                DialRegion.RIGHT -> {
                    viewModel.next()
                }

                DialRegion.BOTTOM -> {
                    viewModel.resetTimer()
                }

                else -> {
                }
            }
        }
    )

    val gestureModifier = state.let {
        Modifier
            .pointerInput(it) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    it.onDown()
                    var change =
                        awaitTouchSlopOrCancellation(pointerId = down.id) { change, _ ->
                            change.consume()
                        }
                    while (change != null && change.pressed) {
                        change = awaitDragOrCancellation(change.id)?.also { inputChange ->
                            if (inputChange.pressed && timerUiState.isActive) {
                                state.onDrag(dragAmount = inputChange.positionChange())
                            }
                        }
                    }
                    it.onRelease()
                }
            }
    }

    val alphaModifier = Modifier.graphicsLayer {
        alpha = if (state.isDragging) 0.38f else 1f
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MainTimerView(
            modifier = alphaModifier,
            state = state,
            gestureModifier = gestureModifier,
            timerUiState = timerUiState,
            timerStyle = timerStyle,
            label = label,
            onStart = viewModel::startTimer,
            onToggle = viewModel::toggleTimer
        )

        DialControl(
            state = state,
            dialContent = { region ->
                DialControlButton(selected = region == state.selectedOption, region = region)
            }
        )
    }
}
