package com.apps.adrcotfas.goodtime.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.apps.adrcotfas.goodtime.common.screenHeight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun ScreensaverMode(
    screensaverMode: Boolean,
    isActive: Boolean,
    screenWidth: Dp,
    yOffset: Animatable<Float, AnimationVector1D>
) {
    val coroutineScope = rememberCoroutineScope()
    var animationJob: Job? by remember { mutableStateOf(null) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(screensaverMode && isActive) {
        if (screensaverMode && isActive) {
            animationJob = coroutineScope.launch {
                while (true) {
                    delay(30.seconds)
                    val max = (configuration.screenHeight - screenWidth) / 3
                    val newOffset =
                        Random.nextInt(from = -max.value.toInt(), until = max.value.toInt())
                    yOffset.animateTo(
                        newOffset.toFloat(),
                        animationSpec = tween(durationMillis = 2000, easing = EaseInOut)
                    )
                }
            }
        } else {
            animationJob?.cancel()
            yOffset.animateTo(
                0f,
                animationSpec = tween(durationMillis = 150, easing = EaseInOut)
            )
        }
    }
}