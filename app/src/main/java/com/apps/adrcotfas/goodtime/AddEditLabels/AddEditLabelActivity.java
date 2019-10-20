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

package com.apps.adrcotfas.goodtime.AddEditLabels;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Label;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.Main.SimpleItemTouchHelperCallback;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityAddEditLabelsBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.takisoft.colorpicker.ColorPickerDialog;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.COLOR_INDEX_UNLABELED;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.clearFocusEditText;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.requestFocusEditText;

public class AddEditLabelActivity extends AppCompatActivity
        implements AddEditLabelsAdapter.OnEditLabelListener{

    private static final String TAG = AddEditLabelActivity.class.getSimpleName();

    private LabelsViewModel mLabelsViewModel;
    private List<Label> mLabels;

    private RecyclerView mRecyclerView;
    private AddEditLabelsAdapter mCustomAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private Label mLabelToAdd;

    private LinearLayout mEmptyState;
    private EditText mAddLabelView;
    private FrameLayout mImageRightContainer;
    private ImageView mImageLeft;
    private FrameLayout mImageLeftContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);

        ActivityAddEditLabelsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_labels);
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);

        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = binding.labelList;
        mEmptyState = binding.emptyState;
        mAddLabelView = binding.addLabel.text;
        mImageRightContainer = binding.addLabel.imageRightContainer;
        mImageLeft = binding.addLabel.imageLeft;
        mImageLeftContainer = binding.addLabel.imageLeftContainer;

        final LiveData<List<Label>> labelsLiveData = mLabelsViewModel.getAllLabels();
        labelsLiveData.observe(this, labels -> {

            mLabels = labels;

            mCustomAdapter = new AddEditLabelsAdapter(this, mLabels, this);
            mRecyclerView.setAdapter(mCustomAdapter);

            LinearLayoutManager lm = new LinearLayoutManager(this);
            lm.setReverseLayout(true);
            lm.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(lm);

            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            labelsLiveData.removeObservers(AddEditLabelActivity.this);

            binding.progressBar.setVisibility(View.GONE);
            updateRecyclerViewVisibility();

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCustomAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        });

        mLabelToAdd = new Label("", COLOR_INDEX_UNLABELED);

        mImageRightContainer.setOnClickListener(view -> {
            addLabel();
            updateRecyclerViewVisibility();
            clearFocusEditText(mAddLabelView, this);
        });

        mImageLeftContainer.setOnClickListener(v -> requestFocusEditText(mAddLabelView, this));

        mAddLabelView.setOnFocusChangeListener((view, hasFocus) -> {
            mImageRightContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            mImageLeft.setImageDrawable(getResources().getDrawable(hasFocus ? R.drawable.ic_palette : R.drawable.ic_add));
            mImageLeftContainer.setOnClickListener(hasFocus ? v -> {
                final ColorPickerDialog.Params p = new ColorPickerDialog.Params.Builder(AddEditLabelActivity.this)
                        .setColors(ThemeHelper.getPalette(this))
                        .setSelectedColor(ThemeHelper.getColor(this, mLabelToAdd.colorId))
                        .build();
                ColorPickerDialog dialog = new ColorPickerDialog(AddEditLabelActivity.this, R.style.DialogTheme, c
                        -> {
                    mLabelToAdd.colorId = ThemeHelper.getIndexOfColor(this, c);
                    mImageLeft.setColorFilter(c);
                }, p);
                dialog.setTitle(R.string.label_select_color);
                dialog.show();
            } : v -> requestFocusEditText(mAddLabelView, this));
            mImageLeft.setColorFilter(ThemeHelper.getColor(this, hasFocus ? mLabelToAdd.colorId : COLOR_INDEX_UNLABELED) );
            if (!hasFocus) {
                mLabelToAdd = new Label("", ThemeHelper.getColor(this, COLOR_INDEX_UNLABELED));
                mAddLabelView.setText("");
            }
        });

        mAddLabelView.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                addLabel();
                updateRecyclerViewVisibility();
                clearFocusEditText(mAddLabelView, this);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDragStarted(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onEditLabel(String label, String newLabel) {
        mLabelsViewModel.editLabelName(label, newLabel);

        Label crtLabel = PreferenceHelper.getCurrentSessionLabel();
        if (crtLabel.title != null && crtLabel.title.equals(label)) {
            PreferenceHelper.setCurrentSessionLabel(new Label(newLabel, crtLabel.colorId));
        }
    }

    @Override
    public void onEditColor(String label, int color) {
        mLabelsViewModel.editLabelColor(label, color);

        Label crtLabel = PreferenceHelper.getCurrentSessionLabel();
        if (crtLabel.title != null && crtLabel.title.equals(label)) {
            PreferenceHelper.setCurrentSessionLabel(new Label(label, color));
        }
    }

    @Override
    public void onToggleArchive(Label label, int adapterPosition) {
        mLabelsViewModel.toggleLabelArchive(label.title, label.archived);

        Label crtLabel = PreferenceHelper.getCurrentSessionLabel();
        if (label.archived && crtLabel.title != null && crtLabel.title.equals(label.title)) {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(null);
            PreferenceHelper.setCurrentSessionLabel(new Label(null, COLOR_INDEX_UNLABELED));
        }
        if (label.archived && !PreferenceHelper.getArchivedLabelHintWasShown()) {
            showArchivedLabelHint();
        }
    }

    /**
     * When archiving a label for the first time, a snackbar will be shown to explain the action.
     */
    private void showArchivedLabelHint() {
        Snackbar s = Snackbar.make(mRecyclerView, getString(R.string.tutorial_archive_label), Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", view -> {
                    PreferenceHelper.setArchivedLabelHintWasShown(true);
                })
                .setActionTextColor(getResources().getColor(R.color.teal200));

        s.setBehavior(new BaseTransientBottomBar.Behavior() {
            @Override
            public boolean canSwipeDismissView(View child) {
                return false;
            }
        });
        TextView tv = s.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        s.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteLabel(Label label, int position) {
        mLabels.remove(label);
        mCustomAdapter.notifyItemRemoved(position);
        mLabelsViewModel.deleteLabel(label.title);

        // workaround for edge case: clipping animation of last cached entry
        if (mCustomAdapter.getItemCount() == 0) {
            mCustomAdapter.notifyDataSetChanged();
        }

        String crtSessionLabel = GoodtimeApplication.getCurrentSessionManager().getCurrentSession().getLabel().getValue();
        if (crtSessionLabel != null && crtSessionLabel.equals(label.title)) {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(null);
        }

        // the label attached to the current session was deleted
        if (label.title.equals(PreferenceHelper.getCurrentSessionLabel().title)) {
            PreferenceHelper.setCurrentSessionLabel(new Label(null, COLOR_INDEX_UNLABELED));
        }

        updateRecyclerViewVisibility();
    }

    /**
     * Update the order of the labels inside the database based on the
     * rearrangement that was done inside the adapter.
     */
    @Override
    public void onLabelRearranged() {
        for (int i = 0; i < mLabels.size(); ++i) {
            mLabelsViewModel.editLabelOrder(mLabels.get(i).title, i);
        }
    }

    private void updateRecyclerViewVisibility() {
        if (mCustomAdapter.getItemCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyState.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Used for checking label names before adding or renaming existing ones.
     * Checks for invalid strings like empty ones, spaces or duplicates
     * @param newLabel The desired name for the new label or renamed label
     * @return true if the label name is valid, false otherwise
     */
    public static boolean labelIsGoodToAdd(Context context, List<Label> labels, String newLabel, String beforeEdit) {
        boolean result = true;

        if (beforeEdit != null && beforeEdit.equals(newLabel)) {
            result = false;
        } else if (newLabel.length() == 0) {
            result = false;
        } else {
            boolean duplicateFound = false;
            for (Label l : labels) {
                if (newLabel.equals(l.title)) {
                    duplicateFound = true;
                    break;
                }
            }
            if (duplicateFound) {
                Toast.makeText(context, R.string.label_already_exists, Toast.LENGTH_SHORT).show();
                result = false;
            }
        }
        return result;
    }

    private void addLabel() {
        mLabelToAdd = new Label(mAddLabelView.getText().toString().trim(), mLabelToAdd.colorId);
        if (labelIsGoodToAdd(this, mLabels, mLabelToAdd.title, null)) {
            mLabels.add(mLabelToAdd);

            mCustomAdapter.notifyItemInserted(mLabels.size());
            mRecyclerView.scrollToPosition(mLabels.size() - 1);

            mLabelsViewModel.addLabel(mLabelToAdd);
            mLabelToAdd = new Label("", COLOR_INDEX_UNLABELED);
            mAddLabelView.setText("");
        }
    }
}
