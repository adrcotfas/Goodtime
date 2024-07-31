package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RowWithText(
    title: String,
    subtitle: String? = null,
    value: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    clickable: Boolean = true,
    onClick: () -> Unit
) {
    val modifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            subtitle?.let {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                    if (clickable) textColor.copy(alpha = 0.75f)
                    else textColor
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

@Preview
@Composable
fun RowWithTextPreview() {
    RowWithText(
        title = "Title",
        subtitle = "Subtitle",
        value = "Value",
        onClick = {}
    )
}