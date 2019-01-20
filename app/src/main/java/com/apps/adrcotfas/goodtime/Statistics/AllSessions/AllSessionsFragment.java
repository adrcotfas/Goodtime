/*
 * Copyright 2016-2019 Adrian Cotfas
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

package com.apps.adrcotfas.goodtime.Statistics.AllSessions;

import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Statistics.Main.RecyclerItemClickListener;
import com.apps.adrcotfas.goodtime.Statistics.Main.SelectLabelDialog;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Upgrade.UpgradeActivity;
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentAllSessionsBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.apps.adrcotfas.goodtime.Statistics.Main.StatisticsActivity.DIALOG_SELECT_LABEL_TAG;

public class AllSessionsFragment extends Fragment implements SelectLabelDialog.OnLabelSelectedListener {

    private AllSessionsAdapter mAdapter;
    private ActionMode mActionMode;
    private List<Long> mSelectedEntries = new ArrayList<>();
    private boolean mIsMultiSelect = false;
    private Menu mMenu;
    private SessionViewModel mSessionViewModel;
    private LabelsViewModel mLabelsViewModel;
    private Session mSessionToEdit;
    private List<Session> mSessions;
    private TextView mEmptyState;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StatisticsFragmentAllSessionsBinding binding = DataBindingUtil.inflate(inflater, R.layout.statistics_fragment_all_sessions, container, false);

        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
        mLabelsViewModel = ViewModelProviders.of(getActivity()).get(LabelsViewModel.class);

        mEmptyState = binding.emptyState;
        mProgressBar = binding.progressBar;

        View view = binding.getRoot();

        mRecyclerView = binding.mainRecylcerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));

        mLabelsViewModel.getLabels().observe(this, labels -> {
            mAdapter = new AllSessionsAdapter(labels);
            mRecyclerView.setAdapter(mAdapter);

            mLabelsViewModel.crtExtendedLabel.observe(this, labelAndColor -> refreshCurrentLabel());

            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            mRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
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
        });
        return view;
    }

    private void refreshCurrentLabel() {
        if (mLabelsViewModel.crtExtendedLabel.getValue() != null && mAdapter != null) {
            if (mLabelsViewModel.crtExtendedLabel.getValue().label.equals(getString(R.string.label_all))) {
                mSessionViewModel.getAllSessionsByEndTime().observe(this, sessions -> {
                    mAdapter.setData(sessions);
                    mSessions = sessions;
                    updateRecyclerViewVisibility();
                });
            } else if (mLabelsViewModel.crtExtendedLabel.getValue().label.equals("unlabeled")) {
                mSessionViewModel.getAllSessionsUnlabeled().observe(this, sessions -> {
                    mAdapter.setData(sessions);
                    mSessions = sessions;
                    updateRecyclerViewVisibility();
                });
            } else {
                mSessionViewModel.getSessions(mLabelsViewModel.crtExtendedLabel.getValue().label).observe(this, sessions -> {
                    mAdapter.setData(sessions);
                    mSessions = sessions;
                    updateRecyclerViewVisibility();
                });
            }
        }
    }

    private void multiSelect(int position) {
        Session s = mAdapter.mEntries.get(position);
        if (s != null) {
            if (mActionMode != null) {
                if (mSelectedEntries.contains(s.id)) {
                    mSelectedEntries.remove(s.id);
                }  else {
                    mSelectedEntries.add(s.id);
                }
                if (mSelectedEntries.size() == 1) {
                    mMenu.getItem(0).setIcon(R.drawable.ic_edit);
                    mActionMode.setTitle(String.valueOf(mSelectedEntries.size()));
                } else if (mSelectedEntries.size() > 1) {
                    mMenu.getItem(0).setIcon(R.drawable.ic_label);
                    mActionMode.setTitle(String.valueOf(mSelectedEntries.size()));
                }  else {
                    mActionMode.setTitle("");
                    mActionMode.finish();
                }
                mAdapter.setSelectedItems(mSelectedEntries);

                // hack bellow to avoid multiple dialogs because of observe
                if (mSelectedEntries.size() == 1) {
                    final Long sessionId = mAdapter.mSelectedEntries.get(0);
                    mSessionViewModel.getSession(sessionId).observe(AllSessionsFragment.this, session -> mSessionToEdit = session);
                }
            }
        }
    }

    private void deleteSessions() {
        for (Long i : mAdapter.mSelectedEntries) {
            mSessionViewModel.deleteSession(i);
        }
        mAdapter.mSelectedEntries.clear();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private void selectAll() {
        mSelectedEntries.clear();
        for (int i = 0; i < mSessions.size(); ++i) {
            mSelectedEntries.add(i, mSessions.get(i).id);
        }

        if (mSelectedEntries.size() == 1) {
            mMenu.getItem(0).setIcon(R.drawable.ic_edit);
            mActionMode.setTitle(String.valueOf(mSelectedEntries.size()));
            mAdapter.setSelectedItems(mSelectedEntries);
        } else if (mSelectedEntries.size() > 1) {
            mMenu.getItem(0).setIcon(R.drawable.ic_label);
            mActionMode.setTitle(String.valueOf(mSelectedEntries.size()));
            mAdapter.setSelectedItems(mSelectedEntries);
        }  else {
            mActionMode.setTitle("");
            mActionMode.finish();
        }
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

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
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.action_edit:
                    if (PreferenceHelper.isPro()) {
                        if (mSelectedEntries.size() > 1) {
                            SelectLabelDialog.newInstance(
                                    AllSessionsFragment.this,
                                    null, false)
                                    .show(fragmentManager, DIALOG_SELECT_LABEL_TAG);

                        } else if (mSessionToEdit != null) {
                            AddEditEntryDialog newFragment = AddEditEntryDialog.newInstance(mSessionToEdit);
                            newFragment.show(fragmentManager, DIALOG_SELECT_LABEL_TAG);
                            mActionMode.finish();
                        }
                    } else {
                        UpgradeActivity.launchUpgradeActivity(getActivity());
                    }
                    break;
                case R.id.action_select_all:
                    selectAll();
                    break;
                case R.id.action_delete:
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.delete_selected_entries)
                            .setPositiveButton(android.R.string.ok, (dialog, id) -> deleteSessions())
                            .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.cancel())
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

    private void updateRecyclerViewVisibility() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            mProgressBar.setVisibility(View.GONE);
            if (mSessions != null && mSessions.isEmpty()) {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyState.setVisibility(View.VISIBLE);
            }
            else {
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyState.setVisibility(View.GONE);
            }
        }, 200);
    }

    @Override
    public void onLabelSelected(LabelAndColor labelAndColor) {
        final String label = labelAndColor.label.equals("unlabeled") ? null : labelAndColor.label;
        for (Long i : mSelectedEntries) {
            mSessionViewModel.editLabel(i, label);
        }
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }
}