package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
private fun ListItemDefaults.enabledColors(): ListItemColors {
    val secondaryColor = colors().headlineColor.copy(alpha = 0.75f)
    return colors(
        supportingColor = secondaryColor,
        trailingIconColor = secondaryColor
    )
}

@Composable
private fun ListItemDefaults.disabledColors(): ListItemColors {
    val disabledColor = colors().disabledHeadlineColor
    return colors(
        headlineColor = disabledColor,
        supportingColor = disabledColor,
        trailingIconColor = disabledColor
    )
}

@Composable
private fun ListItemDefaults.selectedColors(): ListItemColors {
    return colors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
}

@Composable
fun BetterListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    trailing: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    BetterListItem(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        trailing = {
            Text(
                trailing,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
fun BetterListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val internalModifier = modifier.padding(vertical = 4.dp)
    ListItem(
        modifier = if (enabled && onClick != null) Modifier
            .clickable(onClick = onClick)
            .then(internalModifier) else internalModifier,
        colors = if (enabled) ListItemDefaults.enabledColors() else ListItemDefaults.disabledColors(),
        headlineContent = { Text(text = title) },
        supportingContent = {
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        trailingContent = trailing
    )
}

@Composable
fun BetterListItem(
    title: String,
    supporting: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val modifier = Modifier.padding(vertical = 4.dp)
    ListItem(
        modifier = if (enabled && onClick != null) Modifier
            .clickable(onClick = onClick)
            .then(modifier) else modifier,
        colors = if (enabled) ListItemDefaults.enabledColors() else ListItemDefaults.disabledColors(),
        headlineContent = { Text(text = title) },
        supportingContent = supporting,
    )
}

@Composable
fun IconListItem(
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val modifier = Modifier.padding(vertical = 4.dp)

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = if (isSelected) ListItemDefaults.selectedColors() else ListItemDefaults.enabledColors(),
        headlineContent = { Text(text = title) },
        supportingContent = {
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        leadingContent = icon,
    )
}

@Composable
fun SliderListItem(
    modifier: Modifier = Modifier,
    title: String? = null,
    value: Int,
    icon: @Composable (() -> Unit)? = null,
    min: Int = 1,
    max: Int,
    steps: Int = max - min - 1,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: () -> Unit = { },
    showValue: Boolean = false,
    enabled: Boolean = true
) {
    ListItem(
        modifier = modifier,
        colors = if (enabled) ListItemDefaults.enabledColors() else ListItemDefaults.disabledColors(),
        headlineContent = {
            if (title != null) {
                Text(text = title)
            }
        },
        leadingContent = icon,
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = value.toFloat(),
                    onValueChange = {
                        onValueChange(it.roundToInt())
                    },
                    enabled = enabled,
                    onValueChangeFinished = onValueChangeFinished,
                    steps = steps,
                    valueRange = min.toFloat()..max.toFloat()
                )
                if (showValue) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
    )
}

@Composable
fun CheckboxListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    BetterListItem(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        trailing = {
            Checkbox(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        },
        enabled = enabled,
        onClick = { onCheckedChange(!checked) }
    )
}

@Composable
fun SwitchListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    BetterListItem(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        trailing = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        },
        enabled = enabled,
        onClick = { onCheckedChange(!checked) }
    )
}

@Composable
fun DropdownMenuListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    value: String,
    enabled: Boolean = true,
    dropdownMenuOptions: List<String>,
    onDropdownMenuItemSelected: (Int) -> Unit
) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    BetterListItem(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        trailing = {
            Box {
                Text(text = value, style = MaterialTheme.typography.bodyMedium)
                DropdownMenu(
                    modifier = Modifier
                        .crop(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    shape = MaterialTheme.shapes.medium,
                    expanded = dropdownMenuExpanded,
                    onDismissRequest = { dropdownMenuExpanded = false }) {
                    dropdownMenuOptions.forEachIndexed { index, it ->
                        DropdownMenuItem(
                            modifier = if(it == value) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else Modifier,
                            text = {
                                Text(
                                    text = it,
                                    style = if (it == value) MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ) else MaterialTheme.typography.bodyMedium
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
        },
        onClick = { dropdownMenuExpanded = true },
        enabled = enabled
    )
}

@Composable
fun CircularProgressListItem(
    title: String,
    subtitle: String? = null,
    showProgress: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    BetterListItem(
        title = title,
        subtitle = subtitle,
        trailing = {
            Box(
                modifier = Modifier.size(32.dp)
            ) {
                if (showProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        onClick = onClick,
        enabled = enabled
    )
}

@Preview
@Composable
fun SliderListItemPreview() {
    SliderListItem(
        value = 0,
        icon = {
            Icon(Icons.Default.TextFormat, contentDescription = null)
        },
        min = 0,
        max = 5,
        showValue = false,
        onValueChange = {},
        onValueChangeFinished = {}
    )
}