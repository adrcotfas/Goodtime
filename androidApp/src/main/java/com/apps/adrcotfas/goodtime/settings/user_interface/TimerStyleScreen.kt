package com.apps.adrcotfas.goodtime.settings.user_interface

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.common.prettyName
import com.apps.adrcotfas.goodtime.common.prettyNames
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.labels.add_edit.SliderRow
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.ui.TimerFont
import com.apps.adrcotfas.goodtime.ui.common.CheckboxPreference
import com.apps.adrcotfas.goodtime.ui.common.DropdownMenuPreference
import com.apps.adrcotfas.goodtime.ui.common.SubtleVerticalDivider
import com.apps.adrcotfas.goodtime.ui.timerFontWeights
import com.apps.adrcotfas.goodtime.ui.timerTextAzeretStyle
import com.apps.adrcotfas.goodtime.ui.timerTextStyles
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.math.floor

@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
fun findMaxFontSize(
    text: String, style: TextStyle, containerWidth: Dp
): Float {
    var currentFontSize = style.fontSize
    var textWidth = measureTextWidth(text, style.copy(fontSize = currentFontSize))
    while (textWidth > containerWidth) {
        currentFontSize *= 0.95f
        textWidth = measureTextWidth(text, style.copy(fontSize = currentFontSize))
    }
    return floor(currentFontSize.value)
}

@Composable
fun InitTimerStyle(viewModel: MainViewModel) {
    val timerStyle by viewModel.uiState.map { it.timerStyle }
        .collectAsStateWithLifecycle(TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE))
    val configuration = LocalConfiguration.current

    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val screenWidth = if (isPortrait) configuration.screenWidthDp else configuration.screenHeightDp

    if (timerStyle.minSize != TimerStyleData.INVALID_MIN_SIZE) {
        if (timerStyle.minSize == 0f
            // needs a margin because of possible differences in screenWidth between orientations
            || abs(screenWidth - timerStyle.currentScreenWidth) > 64
        ) {
            val maxContainerWidth = screenWidth.dp - 64.dp * 2
            val timerTextSize = findMaxFontSize("90:00", timerTextAzeretStyle, maxContainerWidth)
            viewModel.initTimerStyle(
                maxSize = timerTextSize.em.value,
                screenWidth = screenWidth
            )
        }
    }
}

//TODO: Try to find a workaround until this is fixed: https://issuetracker.google.com/issues/372044241
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerStyleScreen(viewModel: MainViewModel = koinViewModel(), onNavigateBack: () -> Unit) {

    val timerStyle by viewModel.uiState.map { it.timerStyle }
        .collectAsStateWithLifecycle(TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Timer Style") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (timerStyle.minSize != TimerStyleData.INVALID_MIN_SIZE) {
                    Column {
                        SliderRow(
                            icon = { Icon(Icons.Default.FormatSize, contentDescription = null) },
                            min = timerStyle.minSize.toInt(),
                            max = timerStyle.maxSize.toInt(),
                            value = timerStyle.fontSize.toInt(),
                            onValueChange = {
                                viewModel.setTimerSize(it.toFloat())
                            },
                            showValue = false
                        )
                        SliderRow(
                            icon = { Icon(Icons.Default.FormatBold, contentDescription = null) },
                            min = timerFontWeights.first(),
                            max = timerFontWeights.last(),
                            steps = timerFontWeights.size - 2,
                            value = timerStyle.fontWeight,
                            onValueChange = {
                                viewModel.setTimerWeight(it)
                            },
                            showValue = false
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DropdownMenuPreference(
                                modifier = Modifier.weight(0.5f),
                                title = "Font",
                                value = TimerFont.entries[timerStyle.fontIndex].prettyName(),
                                dropdownMenuOptions = prettyNames<TimerFont>()
                            ) {
                                viewModel.setTimerFont(it)
                            }
                            SubtleVerticalDivider()
                            CheckboxPreference(
                                modifier = Modifier.weight(0.5f),
                                title = "Minutes only",
                                checked = timerStyle.minutesOnly,
                                onCheckedChange = {
                                    viewModel.setTimerMinutesOnly(it)
                                })
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = if (timerStyle.minutesOnly) "25" else "12:34",
                            style = TextStyle(
                                fontFamily = timerTextStyles[timerStyle.fontIndex]!![timerStyle.fontWeight],
                                fontSize = timerStyle.fontSize.em.run { if (timerStyle.minutesOnly) this * 1.5 else this * 1 }
                            )
                        )
                    }
                }
            }
        }
    )
}
