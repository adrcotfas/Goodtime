package com.apps.adrcotfas.goodtime.Statistics;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsAllEntriesBinding;

import java.util.ArrayList;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.statistics_all_entries, container, false);
        View view = binding.getRoot();

        RecyclerView recyclerView = binding.mainRecylcerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new AllEntriesAdapter();
        recyclerView.setAdapter(mAdapter);

        ((AllEntriesActivity) getActivity()).getSessionViewModel().getAllSessions()
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
                if (mSelectedEntries.contains(s.endTime)) {
                    mSelectedEntries.remove(s.endTime);
                }  else {
                    mSelectedEntries.add(s.endTime);
                }
                if (mSelectedEntries.size() > 0) {
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
            ((AllEntriesActivity) getActivity()).getSessionViewModel().deleteSession(i);
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
}