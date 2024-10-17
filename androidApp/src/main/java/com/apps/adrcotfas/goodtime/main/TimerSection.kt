package com.apps.adrcotfas.goodtime.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatMilliseconds
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.shared.R
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import com.apps.adrcotfas.goodtime.ui.localColorsPalette
import com.apps.adrcotfas.goodtime.ui.timerTextStyles
import kotlinx.coroutines.delay

@Composable
fun TimerTextView(
    millis: Long,
    color: Color,
    timerStyle: TimerStyleData,
    isPaused: Boolean,
    onPress: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "timer scale"
    )

    val alpha = remember { Animatable(1f) }
    LaunchedEffect(isPaused) {
        if (isPaused) {
            delay(200)
            alpha.animateTo(
                targetValue = 0.3f, animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(200))
        }
    }

    Text(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha.value)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = {
                    onPress()
                }
            ),
        text = millis.formatMilliseconds(timerStyle.minutesOnly),
        style = TextStyle(
            fontSize = timerStyle.inUseFontSize().em,
            fontFamily = timerTextStyles[timerStyle.fontIndex]!![timerStyle.fontWeight],
            color = color
        ),
    )
}

@Composable
fun CurrentStatusAndLabelSection(
    color: Color,
    labelName: String,
    isBreak: Boolean,
    isActive: Boolean,
    isPaused: Boolean,
    streak: Int,
    sessionsBeforeLongBreak: Int,
    showStatus: Boolean,
    showStreak: Boolean,
    showLabel: Boolean,
) {
    val statusColor = color.copy(alpha = 0.75f)
    val statusBackgroundColor = color.copy(alpha = 0.2f)
    val backgroundColor = color.copy(alpha = 0.3f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        StatusIndicator(
            showStatus = showStatus,
            isPaused = isPaused,
            isBreak = isBreak,
            isActive = isActive,
            color = statusColor,
            backgroundColor = statusBackgroundColor
        )

        StreakIndicator(
            showStreak = showStreak,
            isActive = isActive,
            isBreak = isBreak,
            streak = streak,
            sessionsBeforeLongBreak = sessionsBeforeLongBreak,
            color = statusColor,
            backgroundColor = statusBackgroundColor
        )

        AnimatedVisibility(
            labelName != Label.DEFAULT_LABEL_NAME && showLabel
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(backgroundColor)
                    .padding(horizontal = 12.dp)
                    .wrapContentHeight(),
                text = labelName,
                style = MaterialTheme.typography.labelLarge.copy(color = color)
            )
        }
    }
}

@Composable
fun StatusIndicator(
    showStatus: Boolean,
    isPaused: Boolean,
    isBreak: Boolean,
    isActive: Boolean,
    color: Color,
    backgroundColor: Color
) {
    val alpha = remember(isPaused) { Animatable(1f) }
    LaunchedEffect(isPaused) {
        if (!isPaused) {
            delay(500)
            alpha.animateTo(
                targetValue = 0.3f, animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(200))
        }
    }

    AnimatedVisibility(
        showStatus && isActive,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha.value }
                .padding(horizontal = 4.dp)
                .height(32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(backgroundColor)
                .padding(5.dp)
        ) {
            Crossfade(isBreak, label = "label icon") {
                if (it) {
                    Image(
                        colorFilter = ColorFilter.tint(color),
                        painter = painterResource(R.drawable.ic_break),
                        contentDescription = "",
                    )
                } else {
                    Image(
                        colorFilter = ColorFilter.tint(color),
                        painter = painterResource(R.drawable.ic_status_goodtime),
                        contentDescription = "",
                    )
                }
            }
        }
    }
}

@Composable
fun StreakIndicator(
    showStreak: Boolean,
    isActive: Boolean,
    isBreak: Boolean,
    streak: Int,
    sessionsBeforeLongBreak: Int,
    color: Color,
    backgroundColor: Color
) {
    if (sessionsBeforeLongBreak >= 2) {
        AnimatedVisibility(
            showStreak && isActive,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .defaultMinSize(minHeight = 32.dp, minWidth = 32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(backgroundColor)
                    .padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
            ) {
                val numerator = (streak % sessionsBeforeLongBreak).run {
                    plus(if (!isBreak) 1 else if (this == 0 && streak != 0) sessionsBeforeLongBreak else 0)
                }
                FractionText(
                    modifier = Modifier.align(Alignment.Center),
                    numerator = numerator,
                    denominator = sessionsBeforeLongBreak,
                    color = color
                )
            }
        }
    }
}

@Composable
fun FractionText(
    modifier: Modifier,
    numerator: Int,
    denominator: Int,
    color: Color,
) {
    val superscripts = listOf('⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸')
    val subscripts = listOf('₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈')

    val baseStyle =
        MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold, color = color, fontSize = 1.2.em,
            letterSpacing = TextUnit(0.0f, TextUnitType.Em)
        ).toSpanStyle()

    val annotatedString = buildAnnotatedString {
        withStyle(baseStyle.copy(letterSpacing = TextUnit(-0.1f, TextUnitType.Em))) {
            append(superscripts[numerator])
        }
        withStyle(baseStyle) {
            append("⁄")
        }
        withStyle(baseStyle.copy(letterSpacing = TextUnit(-0.3f, TextUnitType.Em))) {
            append(subscripts[denominator])
        }
    }

    Text(
        modifier = modifier.then(Modifier.padding(end = 1.dp)),
        text = annotatedString
    )
}

@Preview
@Composable
fun CurrentStatusAndLabelSectionPreview() {
    ApplicationTheme {
        CurrentStatusAndLabelSection(
            color = MaterialTheme.localColorsPalette.colors[13],
            labelName = "Work",
            isBreak = false,
            isActive = true,
            isPaused = false,
            streak = 2,
            sessionsBeforeLongBreak = 3,
            showStatus = true,
            showStreak = true,
            showLabel = true
        )
    }
}