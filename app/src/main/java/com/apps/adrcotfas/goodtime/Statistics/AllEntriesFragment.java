package com.apps.adrcotfas.goodtime.Statistics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllEntriesBinding;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AllEntriesFragment extends Fragment {
    private StatisticsAllEntriesBinding binding;

    private AllEntriesAdapter mAdapter;
    private ActionMode mActionMode;
    private List<Long> mSelectedEntries = new ArrayList<>();
    private boolean mIsMultiSelect = false;
    private Menu mMenu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.statistics_all_entries, container, false);
        View view = binding.getRoot();
        ((AllEntriesActivity) getActivity()).setSupportActionBar(binding.toolbar);
        if (((AllEntriesActivity) getActivity()).getSupportActionBar() != null) {
            ((AllEntriesActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView recyclerView = binding.mainRecylcerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new AllEntriesAdapter();
        recyclerView.setAdapter(mAdapter);

        AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessionsByEndTime()
                .observe(getActivity(), entries -> mAdapter.setData(entries));

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mIsMultiSelect) {
                    multiSelect(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!mIsMultiSelect) {
                    mAdapter.setSelectedItems(new ArrayList<>());
                    mIsMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = getActivity().startActionMode(mActionModeCallback);
                    }
                }
                multiSelect(position);
            }
        }));
        return view;
    }

    public void multiSelect(int position) {
        Session s = mAdapter.mEntries.get(position);
        if (s != null) {
            if (mActionMode != null) {
                if (mSelectedEntries.contains(s.id)) {
                    mSelectedEntries.remove(s.id);
                }  else {
                    mSelectedEntries.add(s.id);
                }
                if (mSelectedEntries.size() == 1) {
                    mMenu.getItem(0).setVisible(true);
                    mActionMode.setTitle(String.valueOf(mSelectedEntries.size()));
                } else if (mSelectedEntries.size() > 1) {
                    mMenu.getItem(0).setVisible(false);
                    mActionMode.setTitle(String.valueOf(mSelectedEntries.size()));
                }  else {
                    mActionMode.setTitle("");
                    mActionMode.finish();
                }
                mAdapter.setSelectedItems(mSelectedEntries);
            }
        }
    }

    private void deleteSessions() {
        for (Long i : mAdapter.mSelectedEntries) {
            AsyncTask.execute(() -> AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().deleteSession(i));
        }
        mAdapter.mSelectedEntries.clear();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.menu_all_entries_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    showEditEntry(mAdapter.mSelectedEntries.get(0));
                    break;
                case R.id.action_delete:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Delete selected entries?")
                            .setPositiveButton("OK", (dialog, id) -> deleteSessions())
                            .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                    .show();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mIsMultiSelect = false;
            mSelectedEntries = new ArrayList<>();
            mAdapter.setSelectedItems(new ArrayList<>());
        }
    };

    public void showEditEntry(Long sessionId) {
        View promptView = getLayoutInflater().inflate(R.layout.dialog_add_entry, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()).setTitle("Edit session");
        alertDialogBuilder.setView(promptView);

        final EditText durationEditText = promptView.findViewById(R.id.duration);
        final SingleDateAndTimePicker picker = promptView.findViewById(R.id.single_day_picker);

        AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getSession(sessionId)
                .observe(this, session -> {
            durationEditText.setText(Long.toString(session.totalTime));
            picker.setDefaultDate(new Date(session.endTime));
        });

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                            String input = durationEditText.getText().toString();
                            if (input.isEmpty()) {
                                Toast.makeText(getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
                            }
                            else {
                                final long duration = Math.min(Long.parseLong(input), 120);
                                if (duration > 0) {

                                    //TODO: replace AsyncTask everywhere with a ViewModel
                                    AsyncTask.execute(() ->
                                    AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel()
                                            .editSession(sessionId, picker.getDate().getTime(), duration, ""));
                                    dialog.dismiss();
                                    mActionMode.finish();
                                } else {
                                    Toast.makeText(getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        (dialog, id) -> { dialog.cancel(); mActionMode.finish();});

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.action_add:
                showAddEntryDialog();
                break;
            case R.id.action_sort_by_date:
                AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessionsByDuration()
                        .removeObservers(getActivity());
                AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessionsByEndTime()
                        .observe(getActivity(), entries -> mAdapter.setData(entries));
                break;
            case R.id.action_sort_by_duration:
                        AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessionsByEndTime()
                        .removeObservers(getActivity());
                AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessionsByDuration()
                        .observe(getActivity(), entries -> mAdapter.setData(entries));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_all_entries, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    // TODO: clean-up
    public void showAddEntryDialog() {
        View promptView = getLayoutInflater().inflate(R.layout.dialog_add_entry, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()).setTitle("Add entry");
        alertDialogBuilder.setView(promptView);

        final EditText durationEditText = promptView.findViewById(R.id.duration);
        final SingleDateAndTimePicker picker = promptView.findViewById(R.id.single_day_picker);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                            String input = durationEditText.getText().toString();
                            if (input.isEmpty()) {
                                Toast.makeText(getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
                            }
                            else {
                                final long duration = Math.min(Long.parseLong(input), 120);
                                if (duration > 0) {
                                    AsyncTask.execute(() -> AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel()
                                            //TODO: extract to string
                                            .addSession(new Session(0, picker.getDate().getTime(), duration, "unlabeled")));
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
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