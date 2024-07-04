package com.apps.adrcotfas.goodtime.labels

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DeleteConfirmationDialog(
    labelToDeleteName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $labelToDeleteName?") },
        text = { Text("Deleting this label will remove it from associated completed sessions.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
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