package com.apps.adrcotfas.goodtime.data.local.backup

import app.cash.sqldelight.db.SqlDriver
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatForBackupFileName
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatToIso8601
import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.di.reinitModulesAtBackupAndRestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class BackupManager(
    private val fileSystem: FileSystem,
    private val dbPath: String,
    private val filesDirPath: String,
    private val sqlDriver: SqlDriver,
    private val timeProvider: TimeProvider,
    private val backupPrompter: BackupPrompter,
    private val localDataRepository: LocalDataRepository,
    private val logger: Logger,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : KoinComponent {

    private val importedTemporaryFileName = "$filesDirPath/last-import"

    init {
        fileSystem.createDirectory(filesDirPath.toPath())
    }

    suspend fun backup(onComplete: (Boolean) -> Unit) {
        try {
            val tmpFilePath =
                "${filesDirPath}/${DB_BACKUP_PREFIX}${timeProvider.now().formatForBackupFileName()}"
            createBackup(tmpFilePath)
            backupPrompter.promptUserForBackup(BackupType.DB, tmpFilePath.toPath())
            onComplete(true)
        } catch (e: Exception) {
            logger.e(e) { "Backup failed" }
            onComplete(false)
        }
    }

    suspend fun backupToCsv(onComplete: (Boolean) -> Unit) {
        try {
            val tmpFilePath =
                "${filesDirPath}/${PREFIX}${timeProvider.now().formatForBackupFileName()}.csv"
            createCsvBackup(tmpFilePath)
            backupPrompter.promptUserForBackup(BackupType.CSV, tmpFilePath.toPath())
            onComplete(true)
        } catch (e: Exception) {
            logger.e(e) { "Backup failed" }
            onComplete(false)
        }
    }

    suspend fun backupToJson(onComplete: (Boolean) -> Unit) {
        try {
            val tmpFilePath =
                "${filesDirPath}/${PREFIX}${timeProvider.now().formatForBackupFileName()}.json"
            createJsonBackup(tmpFilePath)
            backupPrompter.promptUserForBackup(BackupType.JSON, tmpFilePath.toPath())
            onComplete(true)
        } catch (e: Exception) {
            logger.e(e) { "Backup failed" }
            onComplete(false)
        }
    }

    suspend fun restore(onComplete: (Boolean) -> Unit) {
        try {
            backupPrompter.promptUserForRestore(importedTemporaryFileName) {
                if (!isSQLite3File(importedTemporaryFileName.toPath())) {
                    logger.e { "Invalid backup file" }
                    onComplete(false)
                } else {
                    restoreBackup()
                    onComplete(true)
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Restore backup failed" }
            onComplete(false)
        }
    }

    private suspend fun createBackup(tmpFilePath: String) {
        withContext(defaultDispatcher) {
            try {
                sqlDriver.close()
                fileSystem.copy(dbPath.toPath(), tmpFilePath.toPath())
            } finally {
                afterOperation()
            }
        }
    }

    private suspend fun createCsvBackup(tmpFilePath: String) {
        withContext(defaultDispatcher) {
            fileSystem.sink(tmpFilePath.toPath()).buffer().use { sink ->
                sink.writeUtf8("start,end,duration,label,notes,work,archived\n")
                localDataRepository.selectAllSessions().first().forEach { session ->
                    val labelName =
                        if (session.label == Label.DEFAULT_LABEL_NAME) "" else session.label
                    sink.writeUtf8(
                        "${session.startTimestamp.formatToIso8601()}," +
                                "${session.endTimestamp.formatToIso8601()}," +
                                "${session.duration}," +
                                "${labelName}," +
                                "${session.notes ?: ""}," +
                                "${session.isWork}," +
                                "${session.isArchived}\n"
                    )
                }
            }
        }
    }

    private suspend fun createJsonBackup(tmpFilePath: String) {
        withContext(defaultDispatcher) {
            fileSystem.sink(tmpFilePath.toPath()).buffer().use { sink ->
                sink.writeUtf8("[\n")
                localDataRepository.selectAllSessions().first()
                    .forEachIndexed { index, session ->
                        val labelName =
                            if (session.label == Label.DEFAULT_LABEL_NAME) "" else session.label
                        sink.writeUtf8(
                            "{\"start\":${session.startTimestamp.formatToIso8601()}," +
                                    "\"end\":${session.endTimestamp.formatToIso8601()}," +
                                    "\"duration\":${session.duration}," +
                                    "\"label\":\"${labelName}\"," +
                                    "\"notes\":\"${session.notes ?: ""}\"," +
                                    "\"work\":${session.isWork}," +
                                    "\"archived\":${session.isArchived}}"
                        )
                        if (index < localDataRepository.selectAllSessions().first().size - 1) {
                            sink.writeUtf8(",\n")
                        }
                    }
                sink.writeUtf8("\n]")
            }
        }
    }

    private suspend fun restoreBackup() {
        withContext(defaultDispatcher) {
            try {
                sqlDriver.close()
                fileSystem.copy(importedTemporaryFileName.toPath(), dbPath.toPath())
            } finally {
                afterOperation()
            }
        }
    }

    private fun afterOperation() {
        reinitModulesAtBackupAndRestore()
        get<LocalDataRepository>().reinitDatabase(Database(driver = get<SqlDriver>()))
        get<TimerManager>().restart()
    }

    private fun isSQLite3File(filePath: Path): Boolean {
        val header = ("SQLite format 3").encodeUtf8().toByteArray()
        val buffer = ByteArray(header.size)
        fileSystem.source(filePath).buffer().use { source ->
            val count = source.read(buffer)
            return if (count < header.size) false else buffer.contentEquals(header)
        }
    }


    companion object {
        private const val PREFIX = "Goodtime-Productivity-"
        private const val DB_BACKUP_PREFIX = "$PREFIX-Backup-"
    }
}
