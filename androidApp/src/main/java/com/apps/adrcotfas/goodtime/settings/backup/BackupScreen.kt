package com.apps.adrcotfas.goodtime.settings.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.data.backup.RestoreActivityResultLauncherManager
import com.apps.adrcotfas.goodtime.data.local.backup.BackupViewModel
import com.apps.adrcotfas.goodtime.ui.common.CircularProgressPreference
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import org.koin.compose.koinInject

@Composable
fun BackupScreen(
    viewModel: BackupViewModel = koinInject(),
    activityResultLauncherManager: RestoreActivityResultLauncherManager = koinInject()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                activityResultLauncherManager.importCallback(it)
            }
        }
    )

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.STARTED -> {
                activityResultLauncherManager.setImportActivityResultLauncher(restoreBackupLauncher)
            }
            Lifecycle.State.RESUMED, Lifecycle.State.CREATED -> {
                viewModel.clearProgress()
            }

            else -> {
                // do nothing
            }
        }
    }

    LaunchedEffect(uiState.backupResult) {
        uiState.backupResult?.let {
            Toast.makeText(
                context,
                if (it) "Backup completed successfully" else "Backup failed. Please try again",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.clearBackupError()
        }
    }

    LaunchedEffect(uiState.restoreResult) {
        uiState.restoreResult?.let {
            Toast.makeText(
                context,
                if (it) "Restore completed successfully" else "Restore failed. Please try again",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.clearRestoreError()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 64.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        CircularProgressPreference(
            title = "Export backup",
            subtitle = "The file can be imported back",
            showProgress = uiState.isBackupInProgress
        ) {
            viewModel.backup()
        }
        CircularProgressPreference(
            title = "Restore backup",
            showProgress = uiState.isRestoreInProgress
        ) {
            viewModel.restore()
        }
        SubtleHorizontalDivider()
        CircularProgressPreference(
            title = "Export CSV",
            showProgress = uiState.isCsvBackupInProgress
        ) {
            viewModel.backupToCsv()
        }
        CircularProgressPreference(
            title = "Export JSON",
            showProgress = uiState.isJsonBackupInProgress
        ) {
            viewModel.backupToJson()
        }
    }
}