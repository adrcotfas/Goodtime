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

package com.apps.adrcotfas.goodtime.Main;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityAddEditLabelsBinding;
import com.takisoft.colorpicker.ColorPickerDialog;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.clearFocusEditText;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.requestFocusEditText;

public class AddEditLabelActivity extends AppCompatActivity implements AddEditLabelsAdapter.OnEditLabelListener{

    private LabelsViewModel mLabelsViewModel;
    private List<LabelAndColor> mLabelAndColors;

    private RecyclerView mRecyclerView;
    private AddEditLabelsAdapter mCustomAdapter;

    private LabelAndColor mLabelToAdd;

    private TextView mEmptyState;
    private EditText mAddLabelView;
    private ImageButton mImageRight;
    private ImageView mImageLeft;

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
        mImageRight = binding.addLabel.imageRight;
        mImageLeft = binding.addLabel.imageLeft;

        final LiveData<List<LabelAndColor>> labels = mLabelsViewModel.getLabels();
        labels.observe(this, labelAndColors -> {

            mLabelAndColors = labelAndColors;

            mCustomAdapter = new AddEditLabelsAdapter(this, mLabelAndColors, this);
            mRecyclerView.setAdapter(mCustomAdapter);
            updateRecyclerViewVisibility();

            LinearLayoutManager lm = new LinearLayoutManager(this);
            lm.setReverseLayout(true);
            lm.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(lm);

            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            labels.removeObservers(AddEditLabelActivity.this);
        });

        mLabelToAdd = new LabelAndColor("", getResources().getColor(R.color.white));

        mImageRight.setOnClickListener(view -> {
            addLabel();
            updateRecyclerViewVisibility();
            clearFocusEditText(mAddLabelView, this);
        });

        mImageLeft.setOnClickListener(v -> requestFocusEditText(mAddLabelView, this));

        mAddLabelView.setOnFocusChangeListener((view, hasFocus) -> {
            mImageRight.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            mImageLeft.setImageDrawable(getResources().getDrawable(hasFocus ? R.drawable.ic_palette : R.drawable.ic_add));
            mImageLeft.setOnClickListener(hasFocus ? v -> {
                final ColorPickerDialog.Params p = new ColorPickerDialog.Params.Builder(AddEditLabelActivity.this)
                        .setColors(getResources().getIntArray(R.array.labelColors))
                        .setSelectedColor(mLabelToAdd.color)
                        .build();
                ColorPickerDialog dialog = new ColorPickerDialog(AddEditLabelActivity.this, R.style.DialogTheme, c
                        -> {
                    mLabelToAdd.color = c;
                    mImageLeft.setColorFilter(c);
                }, p);
                dialog.setTitle(R.string.select_color_for_label);
                dialog.show();
            } : v -> requestFocusEditText(mAddLabelView, this));
            mImageLeft.setColorFilter(hasFocus ? mLabelToAdd.color : getResources().getColor(R.color.white));
            if (!hasFocus) {
                mLabelToAdd = new LabelAndColor("", getResources().getColor(R.color.white));
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
    public void onEditLabel(String label, String newLabel) {
        mLabelsViewModel.editLabelName(label, newLabel);

        LabelAndColor crtLabel = PreferenceHelper.getCurrentSessionLabel();
        if (crtLabel.label.equals(label)) {
            PreferenceHelper.setCurrentSessionLabel(new LabelAndColor(newLabel, crtLabel.color));
        }
    }

    @Override
    public void onEditColor(String label, int color) {
        mLabelsViewModel.editLabelColor(label, color);

        LabelAndColor crtLabel = PreferenceHelper.getCurrentSessionLabel();
        if (crtLabel.label.equals(label)) {
            PreferenceHelper.setCurrentSessionLabel(new LabelAndColor(label, color));
        }
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
    public void onDeleteLabel(LabelAndColor labelAndColor, int position) {
        mLabelAndColors.remove(labelAndColor);
        mCustomAdapter.notifyItemRemoved(position);
        mLabelsViewModel.deleteLabel(labelAndColor.label);

        // workaround for edge case: clipping animation of last cached entry
        if (mCustomAdapter.getItemCount() == 0) {
            mCustomAdapter.notifyDataSetChanged();
        }

        String crtSessionLabel = GoodtimeApplication.getCurrentSessionManager().getCurrentSession().getLabel().getValue();
        if (crtSessionLabel != null && crtSessionLabel.equals(labelAndColor.label)) {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(null);
        }

        // the current session label was deleted
        if (labelAndColor.label.equals(PreferenceHelper.getCurrentSessionLabel().label)) {
            PreferenceHelper.setCurrentSessionLabel(new LabelAndColor(null, 0));
        }

        updateRecyclerViewVisibility();
    }

    public void updateRecyclerViewVisibility() {
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
    public static boolean labelIsGoodToAdd(Context context, List<LabelAndColor> labels, String newLabel, String beforeEdit) {
        boolean result = true;

        if (beforeEdit != null && beforeEdit.equals(newLabel)) {
            result = false;
        } else if (newLabel.length() == 0) {
            result = false;
        } else {
            boolean duplicateFound = false;
            for (LabelAndColor l : labels) {
                if (newLabel.equals(l.label)) {
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
        mLabelToAdd = new LabelAndColor(mAddLabelView.getText().toString().trim(), mLabelToAdd.color);
        if (labelIsGoodToAdd(this, mLabelAndColors, mLabelToAdd.label, null)) {
            mLabelAndColors.add(mLabelToAdd);

            mCustomAdapter.notifyItemInserted(mLabelAndColors.size());
            mRecyclerView.scrollToPosition(mLabelAndColors.size() - 1);

            mLabelsViewModel.addLabel(mLabelToAdd);
            mLabelToAdd = new LabelAndColor("", getResources().getColor(R.color.white));
            mAddLabelView.setText("");
        }
    }
}
