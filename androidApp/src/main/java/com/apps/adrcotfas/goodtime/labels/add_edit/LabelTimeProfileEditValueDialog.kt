package com.apps.adrcotfas.goodtime.labels.add_edit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.text.isDigitsOnly
import com.apps.adrcotfas.goodtime.data.model.Label

@Composable
fun LabelTimeProfileEditValueDialog(
    labelToEdit: Label,
    data: DialogData,
    onSave: (Label) -> Unit,
    onDismiss: () -> Unit
) {

    val focusRequester = remember { FocusRequester() }

    var value by remember { mutableStateOf(TextFieldValue(data.initialValue.toString())) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val hasValidValue = value.text.isNotEmpty() && value.text.isDigitsOnly()

    LaunchedEffect(isFocused) {
        val endRange = if (isFocused) value.text.length else 0
        value = value.copy(
            selection = TextRange(
                start = 0,
                end = endRange
            )
        )
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = data.title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.text.isDigitsOnly()) {
                        val intValue = newValue.text.toIntOrNull()
                        value = if (intValue != null && intValue > data.maxValue) {
                            TextFieldValue(
                                text = data.maxValue.toString(),
                                selection = TextRange(newValue.text.length)
                            )
                        } else {
                            newValue
                        }
                    }
                },
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color =
                    MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    showKeyboardOnFocus = true
                ),
                shape = MaterialTheme.shapes.medium,
            )
        },
        confirmButton = {
            TextButton(enabled = hasValidValue,
                onClick = {
                    val newValue = value.text.toInt()
                    onSave(updateLabel(labelToEdit, newValue, data.dialogType))
                    onDismiss()
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(id = android.R.string.cancel))
            }
        }
    )
}

private fun updateLabel(label: Label, newValue: Int, dialogType: DialogType): Label {
    return when (dialogType) {
        DialogType.WORK_DURATION -> label.copy(timerProfile = label.timerProfile.copy(workDuration = newValue))
        DialogType.BREAK_DURATION -> label.copy(timerProfile = label.timerProfile.copy(breakDuration = newValue))
        DialogType.LONG_BREAK_DURATION -> label.copy(
            timerProfile = label.timerProfile.copy(
                longBreakDuration = newValue
            )
        )

        DialogType.SESSIONS_BEFORE_LONG_BREAK -> label.copy(
            timerProfile = label.timerProfile.copy(
                sessionsBeforeLongBreak = newValue
            )
        )

        DialogType.WORK_BREAK_RATIO -> label.copy(
            timerProfile = label.timerProfile.copy(
                workBreakRatio = newValue
            )
        )
        else -> label
    }
}