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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.takisoft.colorpicker.ColorPickerDialog;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.apps.adrcotfas.goodtime.Main.AddEditLabelActivity.labelIsGoodToAdd;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.clearFocusEditText;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.requestFocusEditText;

public class AddEditLabelsAdapter extends RecyclerView.Adapter<AddEditLabelsAdapter.ViewHolder> {

    public interface OnEditLabelListener {
        void onEditColor(String label, int newColor);
        void onEditLabel(String label, String newLabel);
        void onDeleteLabel(LabelAndColor labels, int position);
    }

    private LayoutInflater inflater;
    private List<LabelAndColor> mLabels;
    private WeakReference<Context> mContext;
    private OnEditLabelListener mCallback;

    public AddEditLabelsAdapter(Context ctx, List<LabelAndColor> labels, OnEditLabelListener callback){
        inflater = LayoutInflater.from(ctx);
        mContext = new WeakReference<>(ctx);
        mLabels = labels;
        mCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_add_edit_labels_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.text.setText(mLabels.get(position).label);
        holder.imageLeft.setColorFilter(ThemeHelper.getColor(mContext.get(), mLabels.get(position).color));
        holder.imageLeftContainer.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return mLabels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private EditText  text;
        private ImageView imageLeft;
        private ImageView imageRight;

        private FrameLayout imageLeftContainer;
        private FrameLayout imageRightContainer;
        private FrameLayout imageDeleteContainer;

        public ViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            imageLeft = itemView.findViewById(R.id.image_left); // can have the label or the palette icon
            imageRight = itemView.findViewById(R.id.image_right); // can have the edit or the done icon
            imageLeftContainer = itemView.findViewById(R.id.image_left_container);
            imageRightContainer = itemView.findViewById(R.id.image_right_container);
            imageDeleteContainer = itemView.findViewById(R.id.image_delete_container);

            // switch the focus to a different row
            text.setOnFocusChangeListener((view, hasFocus) -> {

                // shrink the textView when we're in edit mode and the delete button appears
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)text.getLayoutParams();
                params.addRule(RelativeLayout.START_OF, hasFocus ? R.id.image_delete_container : R.id.image_right_container);
                text.setLayoutParams(params);

                int position = getAdapterPosition();
                LabelAndColor crtLabel = mLabels.get(position);
                imageLeft.setImageDrawable(ContextCompat.getDrawable(
                        mContext.get(), hasFocus ? R.drawable.ic_palette : R.drawable.ic_label));
                imageLeftContainer.setClickable(hasFocus);
                imageLeft.setColorFilter(ThemeHelper.getColor(mContext.get(), crtLabel.color));

                imageDeleteContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);

                imageRight.setImageDrawable(ContextCompat.getDrawable(
                        mContext.get(), hasFocus ? R.drawable.ic_done : R.drawable.ic_edit));
                // the done button or the edit button (depending on focus)
                imageRightContainer.setOnClickListener(hasFocus
                        ? v -> clearFocusEditText(text, mContext.get())
                        : v -> requestFocusEditText(text, mContext.get()));

                if (!hasFocus) {
                    String newLabelName = text.getText().toString();
                    // save a label when losing focus if any changes were made
                    if (labelIsGoodToAdd(mContext.get(), mLabels, newLabelName, crtLabel.label)) {
                        mCallback.onEditLabel(crtLabel.label, newLabelName);
                        crtLabel.label = newLabelName;
                        notifyItemChanged(position);
                    } else {
                        text.setText(crtLabel.label);
                    }
                }
            });

            // delete a label
            imageDeleteContainer.setOnClickListener(v -> {
                int position = getAdapterPosition();
                clearFocusEditText(text, mContext.get());
                new AlertDialog.Builder(mContext.get())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.dialog_delete, (dialogInterface, which)
                                -> mCallback.onDeleteLabel(mLabels.get(position), position))
                        .setTitle(R.string.label_delete_title)
                        .setMessage(R.string.label_delete_message)
                        .create().show();
            });

            // save the changes by clearing the focus
            text.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    clearFocusEditText(text, mContext.get());
                    return true;
                }
                return false;
            });

            // changing the color of a label
            imageLeftContainer.setOnClickListener(v -> {
                LabelAndColor crtLabel = mLabels.get(getAdapterPosition());
                final ColorPickerDialog.Params p = new ColorPickerDialog.Params.Builder(mContext.get())
                        .setColors(ThemeHelper.getPalette(mContext.get()))
                        .setSelectedColor(ThemeHelper.getColor(mContext.get(), crtLabel.color))
                        .build();
                ColorPickerDialog dialog = new ColorPickerDialog(mContext.get(), R.style.DialogTheme, c
                        -> {
                    mCallback.onEditColor(crtLabel.label, ThemeHelper.getIndexOfColor(mContext.get(), c));
                    imageLeft.setColorFilter(c);
                    crtLabel.color = ThemeHelper.getIndexOfColor(mContext.get(), c);
                }, p);
                dialog.setTitle(R.string.label_select_color);
                dialog.show();
            });

            // the edit button
            imageRightContainer.setOnClickListener(v -> requestFocusEditText(text, mContext.get()));
        }
    }
}
