package com.apps.adrcotfas.goodtime.Backup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Util.FileUtils;
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentBackupBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import static android.app.Activity.RESULT_OK;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatDateAndTime;

public class BackupFragment extends BottomSheetDialogFragment {
    private static final int IMPORT_BACKUP_REQUEST = 0;

    private SessionViewModel mSessionViewModel;

    public BackupFragment() {
        // Empty constructor required for DialogFragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StatisticsFragmentBackupBinding binding = DataBindingUtil.inflate(inflater, R.layout.statistics_fragment_backup, container, false);

        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
        binding.exportBackup.setOnClickListener(view -> exportBackup());
        binding.importBackup.setOnClickListener(view -> importBackup());

        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMPORT_BACKUP_REQUEST && data != null) {
            Uri uri = data.getData();
            if (uri != null && resultCode == RESULT_OK) {
                onImportBackupResult(uri);
            }
        }
    }

    private void importBackup() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Import backup?")
                .setMessage("The current entries will be lost.")
                .setPositiveButton("OK", (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, IMPORT_BACKUP_REQUEST);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .show();
    }

    private void exportBackup() {
        AppDatabase.closeInstance();
        File file = getActivity().getDatabasePath("goodtime-db");
        File destinationPath = new File(getActivity().getFilesDir(), "tmp");
        File destinationFile = new File(destinationPath, "Goodtime-Backup-" + formatDateAndTime(System.currentTimeMillis()));

        Runnable r = () -> {
            if (file.exists()) {
                try {
                    FileUtils.copyFile(file, destinationFile);
                    if (destinationFile.exists()) {
                        Uri fileUri = FileProvider.getUriForFile(getActivity(), "com.apps.adrcotfas.goodtime", destinationFile);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("application/zip");
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        getActivity().startActivity(Intent.createChooser(intent, "Export backup"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void onImportBackupResult(Uri uri) {
        new ImportBackupTask(mSessionViewModel.getApplication()).execute(uri);
    }
}
