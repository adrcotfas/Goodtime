package com.apps.adrcotfas.goodtime.Main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;
import com.takisoft.colorpicker.ColorPickerDialog;

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
    private Context mContext;
    private OnEditLabelListener mCallback;

    public AddEditLabelsAdapter(Context ctx, List<LabelAndColor> labels, OnEditLabelListener callback){
        inflater = LayoutInflater.from(ctx);
        mContext = ctx;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.text.setText(mLabels.get(position).label);
        holder.imageLeft.setColorFilter(mLabels.get(position).color);
        holder.imageLeft.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return mLabels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private EditText  text;
        private ImageView imageLeft;
        private ImageView imageRight;
        private ImageView imageDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            imageLeft = itemView.findViewById(R.id.image_left); // can have the label or the palette icon
            imageDelete = itemView.findViewById(R.id.image_delete);
            imageRight = itemView.findViewById(R.id.image_right); // can have the edit or the done icon

            // switch the focus to a different row
            text.setOnFocusChangeListener((view, hasFocus) -> {

                int position = getAdapterPosition();
                LabelAndColor crtLabel = mLabels.get(position);
                imageLeft.setImageDrawable(ContextCompat.getDrawable(
                        mContext, hasFocus ? R.drawable.ic_palette : R.drawable.ic_label));
                imageLeft.setClickable(hasFocus);
                imageLeft.setColorFilter(crtLabel.color);

                imageDelete.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);

                imageRight.setImageDrawable(ContextCompat.getDrawable(
                        mContext, hasFocus ? R.drawable.ic_done : R.drawable.ic_edit));
                // the done button or the edit button (depending on focus)
                imageRight.setOnClickListener(hasFocus
                        ? v -> clearFocusEditText(text, mContext)
                        : v -> requestFocusEditText(text, mContext));

                if (!hasFocus) {
                    String newLabelName = text.getText().toString();
                    // save a label when losing focus if any changes were made
                    if (labelIsGoodToAdd(mContext, mLabels, newLabelName, crtLabel.label)) {
                        mCallback.onEditLabel(crtLabel.label, newLabelName);
                        crtLabel.label = newLabelName;
                        notifyItemChanged(position);
                    } else {
                        text.setText(crtLabel.label);
                    }
                }
            });

            // delete a label
            imageDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                clearFocusEditText(text, mContext);
                new AlertDialog.Builder(mContext)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialogInterface, which) -> {
                            mCallback.onDeleteLabel(mLabels.get(position), position);
                        })
                        .setTitle("Delete label?")
                        .setMessage("Deleting this label will remove it from all finished sessions. The sessions will not be removed.")
                        .create().show();
            });

            // save the changes by clearing the focus
            text.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    clearFocusEditText(text, mContext);
                    return true;
                }
                return false;
            });

            // changing the color of a label
            imageLeft.setOnClickListener(v -> {
                LabelAndColor crtLabel = mLabels.get(getAdapterPosition());
                final ColorPickerDialog.Params p = new ColorPickerDialog.Params.Builder(mContext)
                        .setColors(mContext.getResources().getIntArray(R.array.labelColors))
                        .setSelectedColor(crtLabel.color)
                        .build();
                ColorPickerDialog dialog = new ColorPickerDialog(mContext, c
                        -> {
                    mCallback.onEditColor(crtLabel.label, c);
                    imageLeft.setColorFilter(c);
                    crtLabel.color = c;
                }, p);
                dialog.setTitle("Select color");
                dialog.show();
            });

            // the edit button
            imageRight.setOnClickListener(v -> requestFocusEditText(text, mContext));
        }
    }
}
