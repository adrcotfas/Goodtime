package com.apps.adrcotfas.goodtime.settings.permissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AskForPermissionCard(cta: String, description: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                onClick()
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(12.dp).weight(1f),
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(
                onClick = onClick,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = cta)
            }
        }
    }
}

@Preview
@Composable
fun AskForPermissionCardPreview() {
    AskForPermissionCard(
        cta = "Allow",
        description = "For accurate functionality, allow this app to run in the background",
        onClick = {}
    )
}