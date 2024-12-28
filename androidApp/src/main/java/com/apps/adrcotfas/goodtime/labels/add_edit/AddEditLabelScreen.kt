package com.apps.adrcotfas.goodtime.labels.add_edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Label.Companion.LABEL_NAME_MAX_LENGTH
import com.apps.adrcotfas.goodtime.data.model.isDefault
import com.apps.adrcotfas.goodtime.labels.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.labels.main.labelNameIsValid
import com.apps.adrcotfas.goodtime.ui.common.SliderListItem
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.apps.adrcotfas.goodtime.ui.common.clearFocusOnKeyboardDismiss
import com.apps.adrcotfas.goodtime.ui.localColorsPalette
import org.koin.androidx.compose.koinViewModel
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLabelScreen(
    viewModel: LabelsViewModel = koinViewModel(),
    labelName: String,
    showNavigationIcon: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isEditMode = labelName.isNotEmpty()
    val context = LocalContext.current

    LaunchedEffect(labelName) {
        val defaultLabelName = context.getString(R.string.label_default)
        viewModel.init(labelName, defaultLabelName)
    }

    val label = uiState.newLabel
    val isDefaultLabel = label.isDefault()
    val labelNameToDisplay = if (isDefaultLabel) uiState.defaultLabelDisplayName else label.name

    val followDefault = label.useDefaultTimeProfile
    val isCountDown = label.timerProfile.isCountdown
    val isBreakEnabled = label.timerProfile.isBreakEnabled
    val isLongBreakEnabled = label.timerProfile.isLongBreakEnabled

    Scaffold(
        topBar = {
            TopBar(
                onNavigateBack = if (showNavigationIcon) {
                    onNavigateBack
                } else null,
                icon = Icons.Default.Close,
                actions = {
                    if (isDefaultLabel && !label.isSameAs(Label.defaultLabel())) {
                        Button(modifier = Modifier
                            .wrapContentSize()
                            .heightIn(min = 32.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.12f
                                )
                            ),
                            onClick = { viewModel.setNewLabel(Label.defaultLabel()) }) {
                            Text("Reset to default")
                        }
                    }
                    if (uiState.labelToEdit != label) {
                        SaveButton(
                            labelName,
                            label,
                            uiState.labelNameIsValid(),
                            isEditMode,
                            viewModel::updateLabel,
                            viewModel::addLabel,
                            onSave
                        )

                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(!uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding()
            ) {
                LabelNameRow(
                    isDefaultLabel = isDefaultLabel,
                    isAddingNewLabel = !isEditMode,
                    labelName = labelNameToDisplay,
                    onValueChange = {
                        val newLabelName = it.trim()
                        viewModel.setNewLabel(
                            uiState.newLabel.copy(name = newLabelName)
                        )
                    },
                    showError = !uiState.labelNameIsValid()
                )
                ColorSelectRow(selectedIndex = label.colorIndex.toInt()) {
                    viewModel.setNewLabel(label.copy(colorIndex = it.toLong()))
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (!isDefaultLabel) {
                    ListItem(
                        modifier = Modifier.clickable {
                            viewModel.setNewLabel(label.copy(useDefaultTimeProfile = !followDefault))
                        },
                        headlineContent = {
                            Text("Follow default time profile")
                        },
                        trailingContent = {
                            Switch(
                                checked = followDefault,
                                onCheckedChange = {
                                    viewModel.setNewLabel(label.copy(useDefaultTimeProfile = it))
                                }
                            )
                        }
                    )
                }
                AnimatedVisibility(visible = isDefaultLabel || !followDefault) {
                    Column {
                        TimerTypeRow(isCountDown = isCountDown, onCountDownEnabled = {
                            viewModel.setNewLabel(
                                label.copy(
                                    timerProfile = label.timerProfile.copy(isCountdown = it)
                                )
                            )
                        })
                        if (isCountDown) {
                            Column {
                                EditableNumberListItem(
                                    title = "Work duration",
                                    value = label.timerProfile.workDuration,
                                    onValueChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(workDuration = it)
                                            )
                                        )
                                    },
                                )
                                EditableNumberListItem(
                                    title = "Break duration",
                                    value = label.timerProfile.breakDuration,
                                    onValueChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(breakDuration = it)
                                            )
                                        )
                                    },
                                    enableSwitch = true,
                                    switchValue = isBreakEnabled,
                                    onSwitchChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(
                                                    isBreakEnabled = it
                                                )
                                            )
                                        )
                                    }
                                )
                                EditableNumberListItem(
                                    title = "Long break duration",
                                    value = label.timerProfile.longBreakDuration,
                                    onValueChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(
                                                    longBreakDuration = it
                                                )
                                            )
                                        )
                                    },
                                    enabled = isBreakEnabled,
                                    enableSwitch = true,
                                    switchValue = isLongBreakEnabled,
                                    onSwitchChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(
                                                    isLongBreakEnabled = it
                                                )
                                            )
                                        )
                                    }
                                )
                                EditableNumberListItem(
                                    title = "Sessions before long break",
                                    value = label.timerProfile.sessionsBeforeLongBreak,
                                    minValue = 2,
                                    maxValue = 8,
                                    enabled = isBreakEnabled && isLongBreakEnabled,
                                    onValueChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(
                                                    sessionsBeforeLongBreak = it
                                                )
                                            )
                                        )
                                    },
                                )
                            }
                        } else {
                            Column {
                                val toggleBreak = {
                                    viewModel.setNewLabel(
                                        label.copy(
                                            timerProfile = label.timerProfile.copy(isBreakEnabled = !isBreakEnabled)
                                        )
                                    )
                                }
                                ListItem(
                                    modifier = Modifier.clickable {
                                        toggleBreak()
                                    },
                                    headlineContent = {
                                        Text("Enable break budget")
                                    },
                                    trailingContent = {
                                        Checkbox(
                                            checked = isBreakEnabled,
                                            onCheckedChange = { toggleBreak() }
                                        )
                                    }
                                )
                                SliderListItem(title = "Work/break ratio",
                                    min = 1,
                                    max = 5,
                                    enabled = isBreakEnabled,
                                    value = label.timerProfile.workBreakRatio,
                                    showValue = true,
                                    onValueChange = {
                                        viewModel.setNewLabel(
                                            label.copy(
                                                timerProfile = label.timerProfile.copy(
                                                    workBreakRatio = it
                                                )
                                            )
                                        )
                                    })
                            }
                        }
                    }
                }

            }
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
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .animateContentSize()
    ) {
        val internalModifier =
            if (isAddingNewLabel) Modifier.focusRequester(focusRequester) else
                Modifier

        Box {
            BasicTextField(
                modifier = internalModifier
                    .fillMaxWidth()
                    .clearFocusOnKeyboardDismiss(),
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDefaultLabel) null else TextDecoration.Underline
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                readOnly = isDefaultLabel,
                singleLine = true,
                value = labelName,
                onValueChange = {
                    if (it.length <= LABEL_NAME_MAX_LENGTH) {
                        onValueChange(it)
                    }
                },
            )
            if (labelName.isEmpty()) {
                Text(
                    text = if (isDefaultLabel) stringResource(R.string.label_default) else "Add label name",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        AnimatedVisibility(showError && labelName.isNotEmpty()) {
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
fun EditableNumberListItem(
    title: String,
    value: Int,
    minValue: Int = 1,
    maxValue: Int = 90,
    enabled: Boolean = true,
    onValueChange: (Int) -> Unit,
    enableSwitch: Boolean = false,
    switchValue: Boolean = true,
    onSwitchChange: (Boolean) -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value.toString())) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isFocused) {
        val endRange = if (isFocused) textFieldValue.text.length else 0
        textFieldValue = textFieldValue.copy(
            selection = TextRange(
                start = 0,
                end = endRange
            )
        )
    }

    val clickableModifier = if (enabled && switchValue) Modifier.clickable {
        focusRequester.requestFocus()
    } else Modifier

    val colors =
        if (enabled && switchValue) ListItemDefaults.colors() else ListItemDefaults.colors(
            headlineColor = ListItemDefaults.colors().disabledHeadlineColor
        )
    val strokeColor =
        if (enabled && switchValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
            0.38f
        )

    ListItem(
        modifier = clickableModifier,
        colors = colors,
        headlineContent = { Text(text = title) },
        trailingContent = {
            BasicTextField(
                value = textFieldValue,
                enabled = enabled && switchValue,
                interactionSource = interactionSource,
                onValueChange = {
                    if (it.text.length <= 2 && it.text.all { char -> char.isDigit() }) {
                        val newValue = min(max(it.text.toIntOrNull() ?: 0, minValue), maxValue)
                        val empty = it.text.isEmpty()
                        val newText = if (empty) "" else newValue.toString()
                        val newSelection = TextRange(newText.length)
                        textFieldValue = it.copy(text = newText, selection = newSelection)
                        if (!empty) {
                            onValueChange(newValue)
                        }
                    }
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    color = colors.headlineColor
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .widthIn(min = 32.dp, max = 64.dp)
                    .border(1.dp, strokeColor, MaterialTheme.shapes.medium)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .focusRequester(focusRequester)
                    .clearFocusOnKeyboardDismiss {
                        textFieldValue = textFieldValue.copy(
                            text = value.toString()
                        )
                    }
            )
        },
        leadingContent = {
            if (enableSwitch) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = switchValue,
                        enabled = enabled,
                        onCheckedChange = onSwitchChange
                    )
                    VerticalDivider(modifier = Modifier.height(32.dp), color = colors.headlineColor)
                }
            }
        },
    )
}

@Composable
fun SaveButton(
    labelToEditInitialName: String,
    labelToEdit: Label,
    hasValidName: Boolean,
    isEditMode: Boolean,
    onUpdate: (String, Label) -> Unit,
    onAdd: (Label) -> Unit,
    onSave: () -> Unit
) {
    Button(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .padding(horizontal = 8.dp),
        enabled = hasValidName,
        onClick = {
            if (isEditMode) {
                onUpdate(labelToEditInitialName, labelToEdit)
            } else {
                onAdd(labelToEdit)
            }
            onSave()
        }) {
        Text("Save")
    }
}

@Preview
@Composable
fun EditableNumberListItemPreview() {
    EditableNumberListItem(
        title = "Work duration",
        value = 25,
        onValueChange = {},
        enableSwitch = true,
    )
}