package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
@Stable
fun Modifier.hideUnless(condition: Boolean): Modifier {
    val alpha by animateFloatAsState(if (condition) 1f else 0f, label = "hide")
    return this then Modifier.graphicsLayer { this.alpha = alpha }
}