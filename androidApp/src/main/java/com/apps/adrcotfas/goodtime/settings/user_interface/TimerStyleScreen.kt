package com.apps.adrcotfas.goodtime.settings.user_interface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.DomainLabel
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.common.prettyName
import com.apps.adrcotfas.goodtime.common.prettyNames
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.main.MainTimerView
import com.apps.adrcotfas.goodtime.main.TimerUiState
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import com.apps.adrcotfas.goodtime.ui.TimerFont
import com.apps.adrcotfas.goodtime.ui.common.CheckboxListItem
import com.apps.adrcotfas.goodtime.ui.common.DropdownMenuListItem
import com.apps.adrcotfas.goodtime.ui.common.SliderListItem
import com.apps.adrcotfas.goodtime.ui.common.SubtleVerticalDivider
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.apps.adrcotfas.goodtime.ui.lightPalette
import com.apps.adrcotfas.goodtime.ui.timerFontWeights
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

//TODO: Try to find a workaround until this is fixed: https://issuetracker.google.com/issues/372044241
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerStyleScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Boolean,
    showTopBar: Boolean,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isLoading) return

    val timerStyle = uiState.settings.timerStyle

    Scaffold(
        topBar = {
            TopBar(
                isVisible = showTopBar,
                title = "Timer style",
                onNavigateBack = { onNavigateBack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            val randomLabelIndex =
                remember { mutableIntStateOf(-1) }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DropdownMenuListItem(
                        modifier = Modifier.weight(0.5f),
                        title = "Font",
                        value = TimerFont.entries[timerStyle.fontIndex].prettyName(),
                        dropdownMenuOptions = prettyNames<TimerFont>()
                    ) {
                        viewModel.setTimerFont(it)
                    }
                    SubtleVerticalDivider()
                    CheckboxListItem(
                        modifier = Modifier.weight(0.5f),
                        title = "Minutes only",
                        checked = timerStyle.minutesOnly,
                        onCheckedChange = {
                            viewModel.setTimerMinutesOnly(it)
                        })
                }
                SliderListItem(
                    icon = { Icon(Icons.Default.FormatSize, contentDescription = null) },
                    min = timerStyle.minSize.toInt(),
                    max = timerStyle.maxSize.toInt(),
                    value = timerStyle.fontSize.toInt(),
                    onValueChange = {
                        viewModel.setTimerSize(it.toFloat())
                    },
                    showValue = false
                )
                SliderListItem(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CheckboxListItem(
                        modifier = Modifier.weight(0.33f),
                        title = "Status",
                        checked = timerStyle.showStatus,
                        onCheckedChange = {
                            viewModel.setShowStatus(it)
                        })
                    SubtleVerticalDivider()
                    //TODO: add info button to explain what a streak is in this context
                    CheckboxListItem(
                        modifier = Modifier.weight(0.33f),
                        title = "Streak",
                        checked = timerStyle.showStreak,
                        onCheckedChange = {
                            viewModel.setShowStreak(it)
                        })
                    SubtleVerticalDivider()
                    CheckboxListItem(
                        modifier = Modifier.weight(0.33f),
                        title = "Label",
                        checked = timerStyle.showLabel,
                        onCheckedChange = {
                            viewModel.setShowLabel(it)
                        })
                }
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    val oldValue = randomLabelIndex.intValue
                    var newValue = Random.nextInt(lightPalette.lastIndex)
                    while (newValue == oldValue) {
                        newValue = Random.nextInt(lightPalette.lastIndex)
                    }
                    randomLabelIndex.intValue = newValue
                }) {
                    Text("Generate demo label")
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val index =
                    if (randomLabelIndex.intValue == -1) Label.DEFAULT_LABEL_COLOR_INDEX.toInt() else randomLabelIndex.intValue

                val demoLabelNames = listOf(
                    "algebra",
                    "geometry",
                    "calculus",
                    "epigenetics",
                    "astrophysics",
                    "kinetics",
                    "computer vision",
                    "neurobiology",
                    "nutrition",
                    "philosophy",
                    "calligraphy",
                    "surrealism",
                    "meditation",
                    "guitar",
                    "drums",
                    "piano",
                    "thermodynamics",
                    "dermatology",
                    "ecology",
                    "nanophotonics"
                )
                val timerUiState = TimerUiState(
                    baseTime = if (timerStyle.minutesOnly) 25.minutes.inWholeMilliseconds else 25.minutes.plus(
                        34.seconds
                    ).inWholeMilliseconds,
                    timerState = TimerState.RUNNING,
                    sessionsBeforeLongBreak = 4
                )
                assert(lightPalette.lastIndex == demoLabelNames.lastIndex)

                MainTimerView(
                    modifier = Modifier,
                    gestureModifier = Modifier,
                    timerUiState = timerUiState,
                    timerStyle = timerStyle,
                    domainLabel = DomainLabel(label = Label(name = randomLabelIndex.intValue.let {
                        if (it == -1) Label.DEFAULT_LABEL_NAME else demoLabelNames[it]
                    }, colorIndex = index.toLong())),
                    onStart = {},
                    onToggle = { false },
                )
            }
        }
    }
}
