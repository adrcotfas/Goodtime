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

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentAllEntriesBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AllEntriesFragment extends Fragment {
    private StatisticsFragmentAllEntriesBinding binding;

    private AllEntriesAdapter mAdapter;
    private ActionMode mActionMode;
    private List<Long> mSelectedEntries = new ArrayList<>();
    private boolean mIsMultiSelect = false;
    private Menu mMenu;
    private LabelsViewModel mLabelsViewModel;
    //TODO: maybe find an alternative to loading these in onCreateView
    private List<LabelAndColor> mLabels;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.statistics_fragment_all_entries, container, false);

        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        mLabelsViewModel.getLabels().observe(this, labelAndColors -> mLabels = labelAndColors);

        View view = binding.getRoot();
        setHasOptionsMenu(true);
        ((StatisticsActivity) getActivity()).setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (((StatisticsActivity) getActivity()).getSupportActionBar() != null) {
            ((StatisticsActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView recyclerView = binding.mainRecylcerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
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
                    final Long sessionId = mAdapter.mSelectedEntries.get(0);

                    AddEditEntryDialog d = new AddEditEntryDialog(
                            AllEntriesFragment.this,
                            mLabels,
                            true,
                            sessionId,
                            mActionMode);
                    d.show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                    AddEditEntryDialog d = new AddEditEntryDialog(
                            AllEntriesFragment.this,
                            mLabels,
                            false,
                            null,
                            mActionMode);
                    d.show();
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
        super.onCreateOptionsMenu(menu, inflater);
    }
}