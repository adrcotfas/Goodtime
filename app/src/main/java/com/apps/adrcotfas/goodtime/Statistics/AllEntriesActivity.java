package com.apps.adrcotfas.goodtime.Statistics;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.GenericMainBinding;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public class AllEntriesActivity extends AppCompatActivity {

    private SessionViewModel mSessionViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);

        GenericMainBinding binding = DataBindingUtil.setContentView(this, R.layout.generic_main);
        setSupportActionBar(binding.toolbar);

        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Fragment fragment = new AllEntriesFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_entries, menu);
        return true;
    }

    public SessionViewModel getSessionViewModel() {
        return mSessionViewModel;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: implement sort
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddEntryDialog();
                break;
            case R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: clean-up
    public void showAddEntryDialog() {
        View promptView = getLayoutInflater().inflate(R.layout.dialog_add_entry, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this).setTitle("Add entry");
        alertDialogBuilder.setView(promptView);

        final EditText durationEditText = promptView.findViewById(R.id.duration);
        final SingleDateAndTimePicker picker = promptView.findViewById(R.id.single_day_picker);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                    String input = durationEditText.getText().toString();
                    if (input.isEmpty()) {
                        Toast.makeText(AllEntriesActivity.this, "Please enter a valid duration", Toast.LENGTH_LONG).show();
                    }
                    else {
                        final long duration = Math.min(Long.parseLong(input), 120);
                        if (duration > 0) {
                            Session s = new Session();
                            s.totalTime = duration;
                            s.endTime = picker.getDate().getTime();
                            mSessionViewModel.addSession(s);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(AllEntriesActivity.this, "Please enter a valid duration", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                )
                .setNegativeButton("Cancel",
                        (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
