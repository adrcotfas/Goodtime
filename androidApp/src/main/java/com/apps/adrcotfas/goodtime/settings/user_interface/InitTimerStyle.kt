package com.apps.adrcotfas.goodtime.settings.user_interface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.common.screenWidth
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.ui.timerTextAzeretStyle
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
fun InitTimerStyle(viewModel: MainViewModel) {
    val timerStyle by viewModel.uiState.map { it.timerStyle }
        .collectAsStateWithLifecycle(TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE))
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidth

    if (timerStyle.minSize != TimerStyleData.INVALID_MIN_SIZE) {
        if (timerStyle.minSize == 0f
            // needs a margin because of possible differences in screenWidth between orientations
            || abs(screenWidth.value - timerStyle.currentScreenWidth) > 64
        ) {
            val maxContainerWidth = screenWidth - 64.dp * 2
            val timerTextSize = findMaxFontSize("90:00", timerTextAzeretStyle, maxContainerWidth)
            viewModel.initTimerStyle(
                maxSize = timerTextSize.em.value,
                screenWidth = screenWidth.value
            )
        }
    }
}