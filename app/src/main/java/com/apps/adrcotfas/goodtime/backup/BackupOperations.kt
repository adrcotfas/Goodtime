/*
 * Copyright 2016-2021 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleCoroutineScope
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.database.AppDatabase
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.util.FileUtils
import com.apps.adrcotfas.goodtime.util.StringUtils
import com.apps.adrcotfas.goodtime.util.execute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*

class BackupOperations {
    companion object {
        private const val TAG = "BackupOperations"

        fun doImport(scope: LifecycleCoroutineScope, context: Context, uri: Uri) {
            scope.execute(
                onPreExecute = {},
                doInBackground = {
                    lateinit var tmpFile: File
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        inputStream.use {
                            tmpFile = File.createTempFile("import", null, context.filesDir)

                            // first copy the file locally
                            FileUtils.copy(inputStream!!, tmpFile)

                            // some basic checks before importing
                            if (!FileUtils.isSQLite3File(tmpFile)) return@execute false

                            // then use the tmp file to create the db file
                            val inStream = FileInputStream(tmpFile)
                            inStream.use {
                                val destinationPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                                FileUtils.copy(inStream, destinationPath)
                                // recreate database
                                AppDatabase.getDatabase(context)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@execute false
                    } finally {
                        tmpFile.delete()
                    }
                    return@execute true
                },
                onPostExecute = {
                    val success = it
                    val message = context.getString(
                        if (success) R.string.backup_import_success
                        else R.string.backup_import_failed)
                    Log.i(TAG, message)
                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                })
        }

        fun doExport(lifecycleScope: LifecycleCoroutineScope, context: Context) {
            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.closeInstance()
                val file = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                val destinationPath = File(context.filesDir, "tmp")
                val destinationFile = File(
                    destinationPath,
                    "Goodtime-Backup-" + StringUtils.formatDateAndTime(System.currentTimeMillis())
                )
                destinationFile.deleteOnExit()
                if (file.exists()) {
                    try {
                        FileUtils.copyFile(file, destinationFile)
                        if (destinationFile.exists()) {
                            val fileUri = FileProvider.getUriForFile(
                                context,
                                context.packageName,
                                destinationFile
                            )
                            val intent = Intent()
                            intent.action = Intent.ACTION_SEND
                            intent.type = "application/zip"
                            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.backup_export_title)
                                )
                            )

                            // re-open database
                            AppDatabase.getDatabase(context)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.i(TAG, "Backup export failed")
                        Toast.makeText(
                            context,
                            "Backup export failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        fun doExportToCSV(lifecycleScope: LifecycleCoroutineScope, context: Context, sessions: List<Session>) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val destinationPath = File(context.filesDir, "tmp")
                    val output = File(
                        destinationPath, "Goodtime-CSV-" + StringUtils.formatDateAndTime(
                            System.currentTimeMillis()
                        )
                    )
                    output.deleteOnExit()
                    if (!output.parentFile.exists()) output.parentFile.mkdirs()
                    if (!output.exists()) {
                        output.createNewFile()
                    }
                    val fos = FileOutputStream(output)
                    // write header
                    fos.write("time-of-completion".toByteArray())
                    fos.write(",".toByteArray())
                    fos.write("duration".toByteArray())
                    fos.write(",".toByteArray())
                    fos.write("label".toByteArray())
                    fos.write("\n".toByteArray())
                    for (s in sessions) {
                        fos.write(StringUtils.formatDateAndTime(s.timestamp).toByteArray())
                        fos.write(",".toByteArray())
                        fos.write(s.duration.toLong().toString().toByteArray())
                        fos.write(",".toByteArray())
                        fos.write(if (s.label != null) s.label!!.toByteArray() else "".toByteArray())
                        fos.write("\n".toByteArray())
                    }
                    fos.flush()
                    fos.close()
                    if (output.exists()) {
                        val fileUri = FileProvider.getUriForFile(
                            context,
                            context.packageName,
                            output
                        )
                        val intent = Intent()
                        intent.action = Intent.ACTION_SEND
                        intent.type = "text/csv"
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        context
                            .startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.backup_export_CSV)
                                )
                            )
                        // re-open database
                        AppDatabase.getDatabase(context)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}