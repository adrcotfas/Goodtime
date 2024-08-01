package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceGroupTitle(
    text: String,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 24.dp
    )
) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.padding(paddingValues)
    )
}

@Composable
fun CompactPreferenceGroupTitle(text: String) {
    PreferenceGroupTitle(
        text = text, paddingValues = PaddingValues(
            top = 24.dp,
            bottom = 4.dp,
            start = 16.dp,
            end = 16.dp
        )
    )
}

@Composable
fun SubtleHorizontalDivider() {
    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.38f))
}