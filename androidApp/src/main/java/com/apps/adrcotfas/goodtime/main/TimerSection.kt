package com.apps.adrcotfas.goodtime.main

import android.widget.Toast
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.sp
import com.apps.adrcotfas.goodtime.bl.DomainLabel
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatMilliseconds
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.main.dial_control.DialControlState
import com.apps.adrcotfas.goodtime.main.dial_control.DialRegion
import com.apps.adrcotfas.goodtime.shared.R
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import com.apps.adrcotfas.goodtime.ui.common.hideUnless
import com.apps.adrcotfas.goodtime.ui.localColorsPalette
import com.apps.adrcotfas.goodtime.ui.timerTextStyles
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.ShoppingBag
import kotlinx.coroutines.delay

//TODO add another status indicator for the break budget. imagine a bag with a number [ (bag) 3' ]
@Composable
fun MainTimerView(
    modifier: Modifier,
    gestureModifier: Modifier,
    state: DialControlState<DialRegion>? = null,
    timerUiState: TimerUiState,
    timerStyle: TimerStyleData,
    domainLabel: DomainLabel,
    onStart: () -> Unit,
    onToggle: () -> Boolean
) {
    val context = LocalContext.current

    val label = domainLabel.label
    val labelColorIndex = label.colorIndex
    val labelColor = MaterialTheme.localColorsPalette.colors[labelColorIndex.toInt()]
    val isBreak = timerUiState.timerType != TimerType.WORK

    val isCountdown = domainLabel.profile.isCountdown

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CurrentStatusSection(
            Modifier.hideUnless(timerUiState.isActive),
            color = labelColor,
            isBreak = isBreak,
            isActive = timerUiState.isActive,
            isPaused = timerUiState.isPaused,
            isCountdown = isCountdown,
            streak = timerUiState.longBreakData.streak,
            sessionsBeforeLongBreak = timerUiState.sessionsBeforeLongBreak,
            breakBudget = timerUiState.breakBudgetMinutes,
            showStatus = timerStyle.showStatus,
            showStreak = timerStyle.showStreak,
            showBreakBudget = timerStyle.showBreakBudget
        )

        TimerTextView(
            modifier = gestureModifier,
            state = state,
            isPaused = timerUiState.isPaused,
            timerStyle = timerStyle,
            millis = timerUiState.baseTime,
            color = labelColor,
            onPress = {
                if (!timerUiState.isActive) {
                    onStart()
                } else {
                    if (!onToggle()) {
                        Toast.makeText(context, "Cannot pause the break", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })

        LabelSection(
            showLabel = timerStyle.showLabel,
            labelName = label.name,
            color = labelColor
        )
    }
}

@Composable
fun CurrentStatusSection(
    modifier: Modifier = Modifier,
    color: Color,
    isBreak: Boolean,
    isActive: Boolean,
    isPaused: Boolean,
    isCountdown: Boolean,
    streak: Int,
    sessionsBeforeLongBreak: Int,
    breakBudget: Long,
    showStatus: Boolean,
    showStreak: Boolean,
    showBreakBudget: Boolean,
) {
    val statusColor = color.copy(alpha = 0.75f)
    val statusBackgroundColor = color.copy(alpha = 0.15f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .hideUnless(isActive),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        StatusIndicator(
            showStatus = showStatus,
            isPaused = isPaused,
            isBreak = isBreak,
            color = statusColor,
            backgroundColor = statusBackgroundColor
        )
        StreakIndicator(
            showStreak = showStreak && isCountdown,
            isBreak = isBreak,
            streak = streak,
            sessionsBeforeLongBreak = sessionsBeforeLongBreak,
            color = statusColor,
            backgroundColor = statusBackgroundColor
        )
        BreakBudgetIndicator(
            showBreakBudget = showBreakBudget && !isCountdown,
            breakBudget = breakBudget,
            color = statusColor,
            backgroundColor = statusBackgroundColor
        )
    }
}

@Composable
fun StatusIndicator(
    showStatus: Boolean,
    isPaused: Boolean,
    isBreak: Boolean,
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
        showStatus,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        val imageSize = with(LocalDensity.current) {
            MaterialTheme.typography.labelLarge.fontSize.value.sp.toDp() * 2f
        }
        Box(
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha.value }
                .padding(4.dp)
                .size(imageSize)
                .clip(MaterialTheme.shapes.extraSmall)
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
    isBreak: Boolean,
    streak: Int,
    sessionsBeforeLongBreak: Int,
    color: Color,
    backgroundColor: Color
) {
    if (sessionsBeforeLongBreak >= 2) {
        AnimatedVisibility(
            showStreak,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            val imageSize = with(LocalDensity.current) {
                MaterialTheme.typography.labelLarge.fontSize.value.sp.toDp() * 2f
            }
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(imageSize)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(backgroundColor)

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
fun BreakBudgetIndicator(
    showBreakBudget: Boolean,
    breakBudget: Long,
    color: Color,
    backgroundColor: Color
) {
    AnimatedVisibility(
        showBreakBudget,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        val imageSize = with(LocalDensity.current) {
            MaterialTheme.typography.labelLarge.fontSize.value.sp.toDp() * 2f
        }
        Box(
            modifier = Modifier
                .padding(4.dp)
                .height(imageSize)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(backgroundColor)
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(imageSize * 0.65f),
                    imageVector = EvaIcons.Outline.ShoppingBag,
                    contentDescription = "Selected Color",
                    tint = color
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "$breakBudget'",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
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
            fontWeight = FontWeight.Bold, color = color,
            letterSpacing = TextUnit(0.0f, TextUnitType.Sp)
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

@Composable
fun TimerTextView(
    modifier: Modifier,
    state: DialControlState<DialRegion>? = null,
    millis: Long,
    color: Color,
    timerStyle: TimerStyleData,
    isPaused: Boolean,
    onPress: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (state?.isPressed == true) 0.96f else 1f,
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
            .then(modifier)
            .padding(vertical = 8.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha.value)
            .clickable(
                indication = null,
                interactionSource = null,
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
fun LabelSection(showLabel: Boolean, labelName: String, color: Color) {
    val backgroundColor = color.copy(alpha = 0.15f)
    Box(
        modifier = Modifier
            .hideUnless(labelName != Label.DEFAULT_LABEL_NAME && showLabel)
            .padding(horizontal = 4.dp)
            .wrapContentSize()
            .clip(MaterialTheme.shapes.extraSmall)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = labelName,
            style = MaterialTheme.typography.labelLarge.copy(
                color = color,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Preview
@Composable
fun CurrentStatusSectionPreview() {
    ApplicationTheme {
        CurrentStatusSection(
            color = MaterialTheme.localColorsPalette.colors[13],
            isBreak = false,
            isActive = true,
            isPaused = false,
            isCountdown = false,
            streak = 2,
            sessionsBeforeLongBreak = 3,
            breakBudget = 30,
            showStatus = true,
            showStreak = true,
            showBreakBudget = true
        )
    }
}

@Preview
@Composable
fun LabelSectionPreview() {
    ApplicationTheme {
        LabelSection(
            showLabel = true,
            labelName = "Work",
            color = MaterialTheme.localColorsPalette.colors[13]
        )
    }
}