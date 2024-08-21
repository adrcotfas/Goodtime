package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Preference(
    title: String,
    subtitle: String? = null,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable () -> Unit
) {
    val clickableModifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier
    val textColor =
        if (clickable) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = clickableModifier.padding(paddingValues)
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (clickable) subtitleColor else textColor
                )
            }
        }
        content()
    }
}

@Composable
fun SwitchPreference(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Preference(
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun CheckboxPreference(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    clickable: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Preference(
        title = title,
        subtitle = subtitle,
        clickable = clickable,
        onClick = { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun TextPreference(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    clickable: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    onClick: () -> Unit
) {
    Preference(
        title = title,
        subtitle = subtitle,
        clickable = clickable,
        paddingValues = paddingValues,
        onClick = onClick
    ) {
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (clickable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
        }
    }
}

@Composable
fun TextPreferenceWithSeparateSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val clickableModifier = if (checked) Modifier.clickable(onClick = onClick) else Modifier
    val textColor =
        if (checked) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Row(
        modifier = clickableModifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(0.75f), horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked) subtitleColor else textColor
                )
            }
        }
        SubtleVerticalDivider()
        Column(modifier = Modifier.weight(0.25f), horizontalAlignment = Alignment.End) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun TextPreferenceWithDropdownMenu(
    title: String,
    subtitle: String? = null,
    value: String,
    clickable: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    dropdownMenuOptions: List<String>,
    onDropdownMenuItemSelected: (Int) -> Unit
) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    Preference(
        title = title,
        subtitle = subtitle,
        clickable = clickable,
        paddingValues = paddingValues,
        onClick = { dropdownMenuExpanded = true }
    ) {
        Box {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (clickable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            DropdownMenu(
                expanded = dropdownMenuExpanded,
                onDismissRequest = { dropdownMenuExpanded = false }) {
                dropdownMenuOptions.forEachIndexed { index, it ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = it
                            )
                        },
                        onClick = {
                            onDropdownMenuItemSelected(index)
                            dropdownMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun SwitchPreferencePreview() {
    SwitchPreference(
        title = "Title",
        subtitle = "Subtitle",
        checked = true,
        onCheckedChange = {}
    )
}

@Preview
@Composable
fun TextPreferencePreview() {
    TextPreference(
        title = "Title",
        subtitle = "Subtitle",
        value = "Value",
        onClick = {}
    )
}

@Preview
@Composable
fun CheckboxPreferencePreview() {
    CheckboxPreference(
        title = "Title",
        subtitle = "Subtitle",
        checked = true,
        onCheckedChange = {}
    )
}

@Preview
@Composable
fun TextPreferenceWithSeparateSwitchPreview() {
    TextPreferenceWithSeparateSwitch(
        title = "Title",
        subtitle = "Subtitle",
        checked = false,
        onCheckedChange = {},
        onClick = {}
    )
}