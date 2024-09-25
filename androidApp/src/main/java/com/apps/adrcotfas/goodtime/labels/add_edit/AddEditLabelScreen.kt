package com.apps.adrcotfas.goodtime.labels.add_edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Label.Companion.LABEL_NAME_MAX_LENGTH
import com.apps.adrcotfas.goodtime.data.model.isDefault
import com.apps.adrcotfas.goodtime.ui.common.CheckboxPreference
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import com.apps.adrcotfas.goodtime.ui.localColorsPalette

//TODO: consider safe content padding and check bottom row accessibility
@Composable
fun AddEditLabelScreen(
    isEditMode: Boolean,
    labelToEditInitialName: String,
    labelToEdit: Label,
    labelNames: List<String> = emptyList(),
    onEditLabelToEdit: (Label) -> Unit,
    onUpdate: (String, Label) -> Unit,
    onSave: (Label) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDefaultLabel = labelToEdit.isDefault()
    val defaultLabelName = stringResource(R.string.label_default)
    val labelName = labelToEdit.name
    //TODO: don't display the name for default label
    val labelNameToDisplay = if (isDefaultLabel) defaultLabelName else labelName

    var hasValidName by remember { mutableStateOf(labelName.isNotEmpty()) }

    val followDefault = labelToEdit.useDefaultTimeProfile
    val isCountDown = labelToEdit.timerProfile.isCountdown
    val isBreakEnabled = labelToEdit.timerProfile.isBreakEnabled
    val isLongBreakEnabled = labelToEdit.timerProfile.isLongBreakEnabled
    var dialogData by remember { mutableStateOf(DialogData()) }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopButtonsRow(
                isEditMode = isEditMode,
                hasValidName = hasValidName,
                labelToEditInitialName = labelToEditInitialName,
                labelToEdit = labelToEdit,
                onNavigateBack = onNavigateBack,
                onUpdate = onUpdate,
                onSave = onSave
            )

            PreferenceGroupTitle(text = "Name")
            LabelNameRow(
                isDefaultLabel = isDefaultLabel,
                isAddingNewLabel = !isEditMode,
                labelName = labelNameToDisplay,
                onValueChange = {
                    val newLabelName = it.trim()
                    onEditLabelToEdit(labelToEdit.copy(name = newLabelName))
                    hasValidName =
                        !labelNames.map { labels -> labels.lowercase() }
                            .minus(labelName.lowercase())
                            .contains(newLabelName.lowercase()) && newLabelName.lowercase() != defaultLabelName.lowercase()
                },
                showError = !hasValidName
            )

            Spacer(modifier = Modifier.height(8.dp))
            PreferenceGroupTitle(text = "Color")
            ColorSelectRow(selectedIndex = labelToEdit.colorIndex.toInt()) {
                onEditLabelToEdit(labelToEdit.copy(colorIndex = it.toLong()))
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (!isDefaultLabel) {
                SubtleHorizontalDivider()
                CheckboxPreference(title = "Follow Default time profile", checked = followDefault) {
                    onEditLabelToEdit(labelToEdit.copy(useDefaultTimeProfile = it))
                }
                SubtleHorizontalDivider()
            }
            AnimatedVisibility(visible = isDefaultLabel || !followDefault) {
                Column {
                    PreferenceGroupTitle(text = "Timer type")
                    TimerTypeRow(isCountDown = isCountDown) {
                        onEditLabelToEdit(
                            labelToEdit.copy(
                                timerProfile = labelToEdit.timerProfile.copy(isCountdown = it)
                            )
                        )
                    }
                    if (isCountDown) {
                        Column {
                            WorkDurationSliderRow(labelToEdit, onEditLabelToEdit) {
                                dialogData = it
                            }
                            CheckboxPreference(title = "Enable breaks", checked = isBreakEnabled) {
                                onEditLabelToEdit(
                                    labelToEdit.copy(
                                        timerProfile = labelToEdit.timerProfile.copy(isBreakEnabled = it)
                                    )
                                )
                            }
                            AnimatedVisibility(visible = isBreakEnabled) {
                                Column {
                                    BreakDurationSliderRow(labelToEdit, onEditLabelToEdit) {
                                        dialogData = it
                                    }
                                    CheckboxPreference(
                                        title = "Enable long breaks",
                                        checked = isLongBreakEnabled
                                    ) {
                                        onEditLabelToEdit(
                                            labelToEdit.copy(
                                                timerProfile = labelToEdit.timerProfile.copy(
                                                    isLongBreakEnabled = it
                                                )
                                            )
                                        )
                                    }
                                    AnimatedVisibility(visible = isLongBreakEnabled) {
                                        Column {
                                            LongBreakDurationSliderRow(
                                                labelToEdit,
                                                onEditLabelToEdit
                                            ) {
                                                dialogData = it
                                            }
                                            SessionsBeforeLongBreakSliderRow(
                                                labelToEdit,
                                                onEditLabelToEdit
                                            ) {
                                                dialogData = it
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        WorkBreakRationSliderRow(labelToEdit, onEditLabelToEdit) {
                            dialogData = it
                        }
                    }
                }
            }
            AnimatedVisibility(visible = isDefaultLabel && !labelToEdit.isSameAs(Label.defaultLabel())) {
                ResetToDefaultButton(onEditLabelToEdit)
            }
        }
    }
    if (dialogData.dialogType != DialogType.INVALID) {
        LabelTimeProfileEditValueDialog(
            data = dialogData,
            labelToEdit = labelToEdit,
            onSave = onEditLabelToEdit,
            onDismiss = { dialogData = dialogData.copy(dialogType = DialogType.INVALID) },
        )
    }
}

@Composable
private fun TopButtonsRow(
    isEditMode: Boolean,
    hasValidName: Boolean,
    labelToEditInitialName: String,
    labelToEdit: Label,
    onNavigateBack: () -> Unit,
    onUpdate: (String, Label) -> Unit,
    onSave: (Label) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onNavigateBack
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
        Button(
            modifier = Modifier.defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = 32.dp
            ),
            enabled = hasValidName,
            onClick = {
                if (isEditMode) {
                    onUpdate(labelToEditInitialName, labelToEdit)
                } else {
                    onSave(labelToEdit)
                }
                onNavigateBack()
            }) {
            Text("Save")
        }
    }
}

@Composable
private fun LabelNameRow(
    isDefaultLabel: Boolean,
    isAddingNewLabel: Boolean,
    labelName: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
) {

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(labelName) {
        if (isAddingNewLabel) {
            focusRequester.requestFocus()
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        var value by rememberSaveable { mutableStateOf(labelName) }
        val internalModifier =
            if (isAddingNewLabel) Modifier.focusRequester(focusRequester) else Modifier
        Box {
            BasicTextField(
                modifier = internalModifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDefaultLabel) null else TextDecoration.Underline
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                readOnly = isDefaultLabel,
                singleLine = true,
                value = value,
                onValueChange = {
                    if (it.length <= LABEL_NAME_MAX_LENGTH) {
                        value = it
                        onValueChange(it)
                    }
                },
            )
            if (value.isEmpty()) {
                Text(
                    text = if (isDefaultLabel) stringResource(R.string.label_default) else "Add label name",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showError && value.isNotEmpty()) {
            Text(
                text = "Label name already exists",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

        }
    }
}

@Composable
private fun ColorSelectRow(selectedIndex: Int, onClick: (Int) -> Unit) {
    val colors = MaterialTheme.localColorsPalette.colors
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index = selectedIndex)
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(colors) { index, color ->
            LabelColorPickerItem(
                color = color,
                isSelected = index == selectedIndex,
                onClick = {
                    onClick(index)
                }
            )
        }
    }
}

@Composable
private fun LabelColorPickerItem(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected Color",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun TimerTypeRow(isCountDown: Boolean, onCountDownEnabled: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = { onCountDownEnabled(true) },
            label = {
                Text("Countdown")
            },
            selected = isCountDown,
        )

        FilterChip(
            onClick = { onCountDownEnabled(false) },
            label = {
                Text("Count-up")
            },
            selected = !isCountDown,
        )
    }
}

@Composable
private fun WorkDurationSliderRow(
    labelToEdit: Label,
    onEditLabelToEdit: (Label) -> Unit,
    onClick: (DialogData) -> Unit
) {
    val workDurationString = "Work duration"
    val workDuration = labelToEdit.timerProfile.workDuration
    val workDurationMaxValue = 120
    val onSaveWorkDuration: (Int) -> Unit = {
        onEditLabelToEdit(
            labelToEdit.copy(
                timerProfile = labelToEdit.timerProfile.copy(workDuration = it)
            )
        )
    }
    SliderRow(
        title = workDurationString,
        value = workDuration,
        min = 5,
        max = workDurationMaxValue,
        steps = 22,
        onValueChange = onSaveWorkDuration,
        onClick = {
            onClick(
                DialogData(
                    title = workDurationString,
                    dialogType = DialogType.WORK_DURATION,
                    initialValue = workDuration,
                    maxValue = workDurationMaxValue,
                )
            )
        }
    )
}

@Composable
private fun BreakDurationSliderRow(
    labelToEdit: Label,
    onEditLabelToEdit: (Label) -> Unit,
    onClick: (DialogData) -> Unit
) {
    val breakDurationString = "Break duration"
    val breakDuration = labelToEdit.timerProfile.breakDuration
    val breakDurationMaxValue = 60
    val onSaveBreakDuration: (Int) -> Unit = {
        onEditLabelToEdit(
            labelToEdit.copy(
                timerProfile = labelToEdit.timerProfile.copy(
                    breakDuration = it
                )
            )
        )
    }
    SliderRow(
        title = breakDurationString,
        value = breakDuration,
        max = breakDurationMaxValue,
        onValueChange = onSaveBreakDuration,
        onClick = {
            onClick(
                DialogData(
                    title = breakDurationString,
                    dialogType = DialogType.BREAK_DURATION,
                    initialValue = breakDuration,
                    maxValue = breakDurationMaxValue,
                )
            )
        }
    )
}

@Composable
private fun LongBreakDurationSliderRow(
    labelToEdit: Label,
    onEditLabelToEdit: (Label) -> Unit,
    onClick: (DialogData) -> Unit
) {
    val longBreakDurationString = "Long break duration"
    val longBreakDuration =
        labelToEdit.timerProfile.longBreakDuration
    val longBreakDurationMaxValue = 60
    val onSaveLongBreakDuration: (Int) -> Unit = {
        onEditLabelToEdit(
            labelToEdit.copy(
                timerProfile = labelToEdit.timerProfile.copy(
                    longBreakDuration = it
                )
            )
        )
    }
    SliderRow(
        title = longBreakDurationString,
        value = longBreakDuration,
        max = longBreakDurationMaxValue,
        onValueChange = onSaveLongBreakDuration,
        onClick = {
            onClick(
                DialogData(
                    title = longBreakDurationString,
                    dialogType = DialogType.LONG_BREAK_DURATION,
                    initialValue = longBreakDuration,
                    maxValue = longBreakDurationMaxValue,
                )
            )
        }
    )
}

@Composable
private fun SessionsBeforeLongBreakSliderRow(
    labelToEdit: Label,
    onEditLabelToEdit: (Label) -> Unit,
    onClick: (DialogData) -> Unit
) {
    val sessionsBeforeLongBreakString =
        "Sessions before long break"
    val sessionsBeforeLongBreak =
        labelToEdit.timerProfile.sessionsBeforeLongBreak
    val sessionsBeforeLongBreakMinValue = 2
    val sessionsBeforeLongBreakMaxValue = 10
    val onSaveSessionsBeforeLongBreak: (Int) -> Unit = {
        onEditLabelToEdit(
            labelToEdit.copy(
                timerProfile = labelToEdit.timerProfile.copy(
                    sessionsBeforeLongBreak = it
                )
            )
        )
    }
    SliderRow(
        title = sessionsBeforeLongBreakString,
        value = sessionsBeforeLongBreak,
        min = sessionsBeforeLongBreakMinValue,
        max = sessionsBeforeLongBreakMaxValue,
        onValueChange = onSaveSessionsBeforeLongBreak,
        onClick = {
            onClick(
                DialogData(
                    title = sessionsBeforeLongBreakString,
                    dialogType = DialogType.SESSIONS_BEFORE_LONG_BREAK,
                    initialValue = sessionsBeforeLongBreak,
                    minValue = sessionsBeforeLongBreakMinValue,
                    maxValue = sessionsBeforeLongBreakMaxValue,
                )
            )
        }
    )
}

@Composable
private fun WorkBreakRationSliderRow(
    labelToEdit: Label,
    onEditLabelToEdit: (Label) -> Unit,
    onClick: (DialogData) -> Unit
) {

    val workBreakRationString = "Work/Break ratio"
    val workBreakRatio = labelToEdit.timerProfile.workBreakRatio
    val workBreakRationMax = 6
    val onSaveWorkBreakRatio: (Int) -> Unit = {
        onEditLabelToEdit(
            labelToEdit.copy(
                timerProfile = labelToEdit.timerProfile.copy(workBreakRatio = it)
            )
        )
    }
    SliderRow(
        title = workBreakRationString,
        value = workBreakRatio,
        min = 1,
        max = workBreakRationMax,
        onValueChange = onSaveWorkBreakRatio,
        onClick = {
            onClick(
                DialogData(
                    title = workBreakRationString,
                    dialogType = DialogType.WORK_BREAK_RATIO,
                    initialValue = workBreakRatio,
                    maxValue = workBreakRationMax,
                )
            )
        }
    )
}

@Composable
private fun ResetToDefaultButton(onEditLabelToEdit: (Label) -> Unit) {
    Column {
        SubtleHorizontalDivider()
        val interactionSource = remember { MutableInteractionSource() }
        Text(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        onEditLabelToEdit(Label.defaultLabel())
                    })
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Start),
            text = "Reset to default",
            style = MaterialTheme.typography.bodyLarge,
        )
        SubtleHorizontalDivider()
    }
}

enum class DialogType {
    INVALID,
    WORK_DURATION,
    BREAK_DURATION,
    LONG_BREAK_DURATION,
    SESSIONS_BEFORE_LONG_BREAK,
    WORK_BREAK_RATIO
}

data class DialogData(
    val title: String = "",
    val initialValue: Int = 0,
    val minValue: Int = 1,
    val maxValue: Int = 0,
    val dialogType: DialogType = DialogType.INVALID,
)