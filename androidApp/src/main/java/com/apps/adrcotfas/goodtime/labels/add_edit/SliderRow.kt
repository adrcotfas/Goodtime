package com.apps.adrcotfas.goodtime.labels.add_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SliderRow(
    title: String,
    value: Int,
    min: Int = 1,
    max: Int,
    steps: Int = max - min,
    onValueChange: (Int) -> Unit,
    onClick: () -> Unit = {}
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Slider(
                modifier = Modifier.weight(0.9f),
                colors = SliderDefaults.colors().copy(
                    inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant,
                    activeTickColor = MaterialTheme.colorScheme.primary
                ),
                value = value.toFloat(),
                onValueChange = {
                    onValueChange(it.roundToInt())
                },
                steps = steps,
                valueRange = min.toFloat()..max.toFloat()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(0.1f),
                text = value.toString(),
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
fun SliderColumnPreview() {
    var value by remember { mutableIntStateOf(225) }
    SliderRow(title = "Title", value = value, max = 55, onValueChange = { value = it })
}