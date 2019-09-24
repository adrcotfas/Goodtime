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
import android.view.MotionEvent;
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
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.apps.adrcotfas.goodtime.Main.AddEditLabelActivity.labelIsGoodToAdd;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.clearFocusEditText;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.requestFocusEditText;

public class AddEditLabelsAdapter extends RecyclerView.Adapter<AddEditLabelsAdapter.ViewHolder>
        implements ItemTouchHelperAdapter{

    public interface OnEditLabelListener {
        void onEditColor(String label, int newColor);
        void onEditLabel(String label, String newLabel);
        void onDeleteLabel(LabelAndColor labels, int position);
        void onLabelRearranged();
        void onToggleArchive(LabelAndColor label);
        void onDragStarted(RecyclerView.ViewHolder viewHolder);
    }

    private LayoutInflater inflater;
    private List<LabelAndColor> mLabels;
    private WeakReference<Context> mContext;
    private OnEditLabelListener mCallback;

    public AddEditLabelsAdapter(Context ctx,
                                List<LabelAndColor> labels,
                                OnEditLabelListener callback){
        inflater = LayoutInflater.from(ctx);
        mContext = new WeakReference<>(ctx);
        mLabels = labels;
        mCallback = callback;

        // update the order inside
        for (int i = 0; i < mLabels.size(); ++i) {
            mLabels.get(i).order = i;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_add_edit_labels_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        LabelAndColor crtLabel = mLabels.get(position);
        holder.text.setText(crtLabel.title);
        holder.imageLeft.setColorFilter(ThemeHelper.getColor(mContext.get(), crtLabel.colorId));

        holder.labelicon.setImageDrawable(ContextCompat.getDrawable(
                mContext.get(), crtLabel.archived ? R.drawable.ic_label_off : R.drawable.ic_label));

        holder.labelicon.setColorFilter(ThemeHelper.getColor(mContext.get(), crtLabel.colorId));
        holder.scrollIconContainer.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mCallback.onDragStarted(holder);
            }
            return false;
        });
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mLabels, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mLabels, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onClearView() {
        mCallback.onLabelRearranged();
    }

    @Override
    public int getItemCount() {
        return mLabels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        private EditText  text;
        private ImageView labelicon;
        private ImageView imageLeft;
        private ImageView imageRight;

        private FrameLayout scrollIconContainer;
        private FrameLayout imageLeftContainer;
        private FrameLayout labelIconContainer;
        private FrameLayout imageRightContainer;
        private FrameLayout imageDeleteContainer;

        public ViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            labelicon = itemView.findViewById(R.id.label_icon);
            imageLeft = itemView.findViewById(R.id.image_left); // the palette icon
            imageRight = itemView.findViewById(R.id.image_right); // can have the edit or the done icon
            scrollIconContainer = itemView.findViewById(R.id.scroll_icon_container);
            imageLeftContainer = itemView.findViewById(R.id.image_left_container);
            labelIconContainer = itemView.findViewById(R.id.label_icon_container);
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

                labelicon.setColorFilter(ThemeHelper.getColor(mContext.get(), crtLabel.colorId));

                imageLeftContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
                labelIconContainer.setVisibility(hasFocus ? View.INVISIBLE : View.VISIBLE);
                imageDeleteContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);

                imageRight.setImageDrawable(ContextCompat.getDrawable(
                        mContext.get(), hasFocus ? R.drawable.ic_done : R.drawable.ic_edit));
                // the done button or the edit button (depending on focus)
                imageRightContainer.setOnClickListener(hasFocus
                        ? v -> clearFocusEditText(text, mContext.get())
                        : v -> requestFocusEditText(text, mContext.get()));

                if (!hasFocus) {
                    String newLabelName = text.getText().toString();
                    // save a title when losing focus if any changes were made
                    if (labelIsGoodToAdd(mContext.get(), mLabels, newLabelName, crtLabel.title)) {
                        mCallback.onEditLabel(crtLabel.title, newLabelName);
                        crtLabel.title = newLabelName;
                        notifyItemChanged(position);
                    } else {
                        text.setText(crtLabel.title);
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

            // archive and unarchive a label
            labelIconContainer.setOnClickListener(v -> {
                LabelAndColor crtLabel = mLabels.get(getAdapterPosition());
                crtLabel.archived = !crtLabel.archived;
                mCallback.onToggleArchive(crtLabel);
                labelicon.setImageDrawable(ContextCompat.getDrawable(
                        mContext.get(), crtLabel.archived ? R.drawable.ic_label_off : R.drawable.ic_label));
            });

            // changing the colorId of a label
            imageLeftContainer.setOnClickListener(v -> {
                LabelAndColor crtLabel = mLabels.get(getAdapterPosition());
                final ColorPickerDialog.Params p = new ColorPickerDialog.Params.Builder(mContext.get())
                        .setColors(ThemeHelper.getPalette(mContext.get()))
                        .setSelectedColor(ThemeHelper.getColor(mContext.get(), crtLabel.colorId))
                        .build();
                ColorPickerDialog dialog = new ColorPickerDialog(mContext.get(), R.style.DialogTheme, c
                        -> {
                    mCallback.onEditColor(crtLabel.title, ThemeHelper.getIndexOfColor(mContext.get(), c));
                    imageLeft.setColorFilter(c);
                    crtLabel.colorId = ThemeHelper.getIndexOfColor(mContext.get(), c);
                }, p);
                dialog.setTitle(R.string.label_select_color);
                dialog.show();
            });

            // the edit button
            imageRightContainer.setOnClickListener(v -> requestFocusEditText(text, mContext.get()));
        }

        @Override
        public void onItemSelected() {
            //TODO: show the elevation on the view
        }
    }
}
