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
package com.apps.adrcotfas.goodtime.main.dialcontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.main.dialcontrol.DialControlState.Companion.calculateStartAngle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

object DialControlDefaults {
    private const val TRANSLATION_SLOWDOWN_FACTOR = 0.75f

    @Composable
    fun Indicator(state: DialControlState<*>) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    state.indicatorOffset.value.let {
                        translationX = TRANSLATION_SLOWDOWN_FACTOR * it.x
                        translationY = TRANSLATION_SLOWDOWN_FACTOR * it.y
                    }
                }
                .size(state.config.indicatorSize)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                ),
        )
    }
}

data class DialConfig(
    val size: Dp = 0.dp,
    val indicatorSize: Dp = 24.dp,
    val cutoffFraction: Float = 0.4f,
)

@Composable
fun <T> rememberDialControlState(
    options: List<T>,
    onSelected: (T) -> Unit,
    density: Density = LocalDensity.current,
    config: DialConfig = DialConfig(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): DialControlState<T> {
    return remember(density, config, coroutineScope, onSelected) {
        DialControlState(
            initialOptions = options,
            onSelected = onSelected,
            config = config,
            density = density,
            coroutineScope = coroutineScope,
        )
    }
}

@Composable
fun <T> DialControl(
    modifier: Modifier = Modifier,
    state: DialControlState<T>,
    dialContent: @Composable (T) -> Unit,
    indicator: @Composable (DialControlState<T>) -> Unit = {
        DialControlDefaults.Indicator(state)
    },
) {
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(state) {
        val selection = snapshotFlow { state.selectedOption }
        selection
            .zip(selection.drop(1)) { previous, current ->
                if (previous != current && current != null) {
                    HapticFeedbackType.LongPress
                } else {
                    null
                }
            }
            .filterNotNull()
            .collect {
                hapticFeedback.performHapticFeedback(it)
            }
    }

    Box {
        AnimatedVisibility(
            visible = state.isDragging,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .size(state.config.size)
                .align(Alignment.Center),
        ) {
            CircleDial(
                modifier = modifier,
                state = state,
                optionContent = dialContent,
                indicator = { indicator(state) },
            )
        }
    }
}

@Composable
private fun <T> CircleDial(
    modifier: Modifier,
    state: DialControlState<T>,
    optionContent: @Composable (T) -> Unit,
    indicator: @Composable () -> Unit,
) {
    val scales = remember(state.options) {
        state.options.associateWith { Animatable(initialValue = 0f, Float.VectorConverter) }.toMap()
    }

    LaunchedEffect(state.selectedOption, state.options) {
        state.options.forEach { option ->
            launch {
                scales[option]?.animateTo(
                    if (option == state.selectedOption) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                )
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val sweep = 360f / state.options.size
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (state.selectedOption == null) {
                indicator()
            }
        }

        state.options.forEachIndexed { index, option ->
            key(option) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val startAngle =
                                calculateStartAngle(index = index, count = state.options.size)
                            val radians = (startAngle + sweep / 2) * Math.PI / 180
                            val radius =
                                (state.config.size.toPx() / 2) * (state.config.cutoffFraction + (1f - state.config.cutoffFraction) / 2)
                            translationX = (radius * cos(radians)).toFloat()
                            translationY = (radius * sin(radians)).toFloat()
                        },
                ) {
                    optionContent(option)
                }
            }
        }
    }
}

enum class DialRegion(val icon: ImageVector? = null, val label: String? = null) {
    TOP(icon = Icons.Filled.PlusOne, label = "+1 min"),
    RIGHT(icon = Icons.AutoMirrored.Filled.ArrowForwardIos, label = "Skip"),
    BOTTOM(icon = Icons.Filled.Close, "Stop"),
    LEFT(null),
}
