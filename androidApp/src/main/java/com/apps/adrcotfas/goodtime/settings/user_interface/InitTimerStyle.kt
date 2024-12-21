package com.apps.adrcotfas.goodtime.settings.user_interface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.common.screenWidth
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import com.apps.adrcotfas.goodtime.ui.timerTextAzeretStyle
import kotlin.math.abs
import kotlin.math.floor

@Composable
fun InitTimerStyle(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isLoading) return

    val timerStyle = uiState.settings.timerStyle
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidth

    if (timerStyle.fontSize == 0f || abs(screenWidth.value - timerStyle.currentScreenWidth) > 64) {
        val maxContainerWidth = screenWidth * 0.75f
        val timerTextSize = findMaxFontSize(timerTextAzeretStyle, maxContainerWidth)
        viewModel.initTimerStyle(
            maxSize = timerTextSize.em.value,
            screenWidth = screenWidth.value
        )
    }
}

@Composable
private fun findMaxFontSize(style: TextStyle, containerWidth: Dp): Float {
    var currentFontSize = style.fontSize
    var textWidth = measureTextWidth(style.copy(fontSize = currentFontSize))
    while (textWidth > containerWidth) {
        currentFontSize *= 0.95f
        textWidth = measureTextWidth(style.copy(fontSize = currentFontSize))
    }
    return floor(currentFontSize.value)
}

@Composable
private fun measureTextWidth(style: TextStyle): Dp {
    val text = "90:00"
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}