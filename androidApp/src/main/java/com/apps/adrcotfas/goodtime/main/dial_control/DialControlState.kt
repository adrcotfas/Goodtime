package com.apps.adrcotfas.goodtime.main.dial_control

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.atan2

@Stable
class DialControlState<T>(
    initialOptions: List<T>,
    val config: DialConfig,
    private val onSelected: (T) -> Unit,
    private val density: Density,
    private val coroutineScope: CoroutineScope,
) {

    private var disabledOptions by mutableStateOf(emptyList<T>())
    var options by mutableStateOf(initialOptions)
        private set

    var isPressed by mutableStateOf(false)
        private set

    var isDragging by mutableStateOf(false)
        private set

    val indicatorOffset = Animatable(initialValue = Offset.Zero, Offset.VectorConverter)

    val selectedOption: T? by derivedStateOf {
        val sizePx = with(density) { config.size.toPx() }
        val radius = sizePx / 2
        val offset = indicatorOffset.value
        val distance = offset.getDistance()
        if (distance < radius * config.cutoffFraction) {
            null
        } else {
            val degree = (180f / Math.PI) * atan2(y = offset.y, x = offset.x)
            val sweep = 360f / options.size
            val index = options.indices.firstOrNull { index ->
                val startAngle = calculateStartAngle(index, options.size)
                val endAngle = startAngle + sweep
                degree >= startAngle && degree < endAngle
            } ?: options.lastIndex
            options[index].let {
                if (it in disabledOptions) null else it
            }
        }
    }

    fun updateEnabledOptions(options: List<T>) {
        disabledOptions = options
    }

    fun isDisabled(option: T): Boolean {
        return disabledOptions.contains(option)
    }

    fun onDown() {
        isPressed = true
    }

    fun onRelease() {
        isDragging = false
        isPressed = false
        selectedOption?.let(onSelected)
        coroutineScope.launch {
            indicatorOffset.animateTo(Offset.Zero)
        }
    }

    fun onDrag(dragAmount: Offset) {
        isDragging = true
        val origin = indicatorOffset.value
        val target = origin + dragAmount
        val radius = with(density) { config.size.toPx() / 1.75f }
        val distance = target.getDistance()
        val clamped =
            if (distance > radius) target * radius / distance else target
        coroutineScope.launch {
            indicatorOffset.snapTo(clamped)
        }
    }

    companion object {
        internal fun calculateStartAngle(index: Int, count: Int): Float {
            val sweep = 360f / count
            return 0f + sweep * index - 90f - sweep / 2
        }
    }
}