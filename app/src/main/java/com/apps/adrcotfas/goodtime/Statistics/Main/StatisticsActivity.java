package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Statistics.AllSessions.AddEditEntryDialog;
import com.apps.adrcotfas.goodtime.Statistics.AllSessions.AllSessionsFragment;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Util.FileUtils;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.StatisticsActivityMainBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import static android.widget.Toast.LENGTH_SHORT;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatDateAndTime;

public class StatisticsActivity extends AppCompatActivity {

    private static final int IMPORT_BACKUP_REQUEST = 0;
    private LabelsViewModel mLabelsViewModel;
    private ChipGroup mChipGroupLabels;
    private SessionViewModel mSessionViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);
        StatisticsActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.statistics_activity_main);
        mChipGroupLabels = binding.toolbarWrapper.labelView.labels;
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLabelsViewModel.isMainPage.observe(this, isMainPage -> {
            Fragment fragment = isMainPage ? new StatisticsFragment() : new AllSessionsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commitAllowingStateLoss();
            invalidateOptionsMenu();
        });

        setupLabelView();

        if (savedInstanceState == null) {
            Fragment fragment = new StatisticsFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    @SuppressLint("ResourceType")
    private void setupLabelView() {
        mChipGroupLabels.setOnCheckedChangeListener((chipGroup, id) -> {
            // this is called on screen rotation; maybe find a cleaner way
            if (id == -1) {
                return;
            }

            Chip chip = ((Chip) chipGroup.getChildAt(id));
            if (chip == null) {
                chip = chipGroup.findViewById(id);
            }

            for (int i = 0; i < mChipGroupLabels.getChildCount(); ++i) {
                mChipGroupLabels.getChildAt(i).setClickable(true);
            }
            chip.setClickable(false);

            if (mLabelsViewModel.crtExtendedLabel.getValue() != null) {
                switch (mLabelsViewModel.crtExtendedLabel.getValue().label) {
                    case "total":
                        mSessionViewModel.getAllSessionsByEndTime().removeObservers(this);
                        break;
                    case "unlabeled":
                        mSessionViewModel.getAllSessionsUnlabeled().removeObservers(this);
                        break;
                    default:
                        mSessionViewModel.getSessions(mLabelsViewModel.crtExtendedLabel.getValue().label).removeObservers(this);
                        break;
                }
            }

            mLabelsViewModel.crtExtendedLabel.setValue(new LabelAndColor(chip.getText().toString(), chip.getChipBackgroundColor().getDefaultColor()));
        });

        mLabelsViewModel.getLabels().observe(this, labels -> {
            for (int i = 0; i < labels.size(); ++i) {
                Chip chip = new Chip(this);
                chip.setText(labels.get(i).label);
                chip.setChipBackgroundColor(ColorStateList.valueOf(labels.get(i).color));
                chip.setCheckable(true);
                ThemeHelper.styleChip(this, chip);

                chip.setId(i + 1);
                mChipGroupLabels.addView(chip, i + 1);
                if (mLabelsViewModel.crtExtendedLabel.getValue().label.equals(labels.get(i).label)) {
                    chip.setChecked(true);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_list:
                mLabelsViewModel.isMainPage.setValue(!mLabelsViewModel.isMainPage.getValue());
                break;

            case R.id.action_add:
                // TODO: remove this later
//                for (int i = 0; i < 10000; ++i) {
//                    Session session = new Session(
//                            0,
//                            System.currentTimeMillis(),
//                            42,
//                            null);
//
//                    mSessionViewModel.addSession(session);
//                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                AddEditEntryDialog newFragment = new AddEditEntryDialog();
                newFragment.show(fragmentManager, "");
                break;

            case R.id.action_export_backup:
                exportBackup();
                break;

            case R.id.action_import_backup:
                importBackup();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics_main, menu);

        if (mLabelsViewModel.isMainPage.getValue()) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_details));
        } else {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_show_list));
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMPORT_BACKUP_REQUEST) {
            Uri uri = data.getData();
            if (uri != null && resultCode == RESULT_OK) {
                onImportBackupResult(uri);
            }
        }
    }

    private void onImportBackupResult(Uri uri) {
        try {
            InputStream tmpStream = getContentResolver().openInputStream(uri);
            File tmpPath = new File(getFilesDir(), "tmp");
            File tempFile = File.createTempFile("import", "", tmpPath);

            String fileName = null;
            tempFile.deleteOnExit();
            FileUtils.copy(tmpStream, tempFile);

            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }

            // Walking on thin ice but this should suffice for now
            if ((fileName != null && !fileName.contains("Goodtime")) || !FileUtils.isSQLite3File(tempFile)) {
                Toast.makeText(getBaseContext(), "Invalid file", LENGTH_SHORT)
                        .show();
                return;
            }

            FileInputStream inStream = new FileInputStream(tempFile);
            File destinationPath = getDatabasePath("goodtime-db");
            //TODO: copy should be done on a background thread
            FileUtils.copy(inStream, destinationPath);
            //TODO: refresh checkboxes (labels were probably changed)
            //refreshUi();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importBackup() {
        new AlertDialog.Builder(this)
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
        File file = getDatabasePath("goodtime-db");
        File destinationPath = new File(getFilesDir(), "tmp");
        File destinationFile = new File(destinationPath, "Goodtime-Backup-" + formatDateAndTime(System.currentTimeMillis()));

        Runnable r = () -> {
            if (file.exists()) {
                try {
                    FileUtils.copyFile(file, destinationFile);
                    if (destinationFile.exists()) {
                        Uri fileUri = FileProvider.getUriForFile(this, "com.apps.adrcotfas.goodtime", destinationFile);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("application/zip");
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "ceva fin"));
                    } else {
                        Toast.makeText(this, "Dataabase invalid", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }


}
