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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.apps.adrcotfas.goodtime.statistics.SessionViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import android.content.Intent
import android.app.Activity
import com.apps.adrcotfas.goodtime.database.AppDatabase
import android.content.DialogInterface
import android.widget.Toast
import androidx.core.content.FileProvider
import android.net.Uri
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.DialogBackupBinding
import com.apps.adrcotfas.goodtime.util.FileUtils
import com.apps.adrcotfas.goodtime.util.StringUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class BackupFragment : BottomSheetDialogFragment() {
    private lateinit var sessionViewModel: SessionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogBackupBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_backup, container, false)
        sessionViewModel = ViewModelProvider(requireActivity()).get(SessionViewModel::class.java)
        binding.exportBackup.setOnClickListener { exportBackup() }
        binding.importBackup.setOnClickListener { importBackup() }
        binding.exportCsv.setOnClickListener { exportCsv() }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMPORT_BACKUP_REQUEST && data != null) {
            val uri = data.data
            if (uri != null && resultCode == Activity.RESULT_OK) {
                AppDatabase.getDatabase(requireContext())
                BackupOperations.doImport(lifecycleScope, requireContext(), uri)
            }
        }
    }

    private fun importBackup() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.backup_import_title)
            .setMessage(R.string.backup_import_message)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(intent, IMPORT_BACKUP_REQUEST)
            }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .show()
    }

    private fun exportBackup() {
        BackupOperations.doExport(lifecycleScope, requireContext())
    }

    private fun exportCsv() {
        val sessionsLiveData = sessionViewModel.allSessions
        sessionsLiveData.observe(viewLifecycleOwner, { sessions: List<Session> ->
            if (sessions.isEmpty()) {
                    Toast.makeText(
                        requireActivity(),
                        R.string.backup_no_completed_sessions,
                        Toast.LENGTH_SHORT
                    ).show()
                dismiss()
            } else {
                BackupOperations.doExportToCSV(lifecycleScope, requireContext(), sessions)
            }
        })
    }

    companion object {
        private const val IMPORT_BACKUP_REQUEST = 0
    }
}