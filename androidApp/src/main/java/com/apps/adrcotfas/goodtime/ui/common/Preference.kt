package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    value: String,
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (clickable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
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