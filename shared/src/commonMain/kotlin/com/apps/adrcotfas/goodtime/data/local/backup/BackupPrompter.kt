package com.apps.adrcotfas.goodtime.data.local.backup

import okio.Path

enum class BackupType {
    DB, JSON, CSV
}

interface BackupPrompter {
    suspend fun promptUserForBackup(backupType: BackupType, fileToSharePath: Path)
    suspend fun promptUserForRestore(importedFilePath: String, callback: suspend () -> Unit)
}
