package com.apps.adrcotfas.goodtime.Main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;
import com.takisoft.colorpicker.ColorPickerDialog;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

public class EditLabelDialog {

    private Context mContext;
    private String mCurrentLabel;
    private AlertDialog mDialog;
    private AlertDialog.Builder mBuilder;
    private ArrayAdapter<LabelAndColor> mAdapter;
    private List<LabelAndColor> mLabels;

    private EditText    mAddLabelEditText;
    private ImageButton mDoneButton;

    public EditLabelDialog(Context context, List<LabelAndColor> labels, String crtLabel) {
        mContext = context;
        mCurrentLabel = crtLabel;
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle("Select label");
        mBuilder.setPositiveButton("OK", null);
        mBuilder.setNegativeButton("Cancel", (dialogInterface, which) -> mDialog.dismiss());
        mLabels = labels;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogLayout = inflater.inflate(R.layout.dialog_edit_label, null);
        mAddLabelEditText = dialogLayout.findViewById(R.id.add_label_row_text);
        mDoneButton = dialogLayout.findViewById(R.id.add_label_row_done);

        mAddLabelEditText.setOnTouchListener((view, motionEvent) -> {
            mAddLabelEditText.setFocusable(true);
            mAddLabelEditText.setFocusableInTouchMode(true);
            mDoneButton.setVisibility(View.VISIBLE);
            return false;
        });

        mDoneButton.setOnClickListener(v -> {
            onDoneButtonClicked();
            clearFocusOfAddLabelView();
            hideKeyboard(v, context);
        });
        mAddLabelEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDoneButtonClicked();
            }
            clearFocusOfAddLabelView();
            hideKeyboard(v, context);
            return false;
        });

        ListView mListView = dialogLayout.findViewById(R.id.label_list);

        final LabelAndColor unlabeledLabel = new LabelAndColor("unlabeled", mContext.getResources().getColor(R.color.white));
        if (!containsLabel("unlabeled")) {
            labels.add(0, unlabeledLabel);
        }

        mAdapter = new ArrayAdapter<LabelAndColor>(mContext, R.layout.dialog_edit_label_row, R.id.dialog_edit_label_row_radio_button, labels) {

            int selectedPosition = getLabelIndex(crtLabel);
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = LayoutInflater.from(mContext);
                    v = vi.inflate(R.layout.dialog_edit_label_row, parent, false);
                }
                final String selectedLabel = mLabels.get(position).label;

                RadioButton r = v.findViewById(R.id.dialog_edit_label_row_radio_button);
                r.setChecked(position == selectedPosition);
                ImageView moreButton = v.findViewById(R.id.dialog_edit_label_row_more);

                if (selectedLabel.equals("unlabeled")) {
                    moreButton.setVisibility(View.GONE);
                } else {
                    moreButton.setVisibility(View.VISIBLE);
                    moreButton.setOnClickListener(view -> {
                        PopupMenu popup = new PopupMenu(mContext, view);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.menu_edit_labels_row_edit, popup.getMenu());

                        popup.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case R.id.label_row_menu_rename:

                                    LayoutInflater vi = LayoutInflater.from(mContext);
                                    View renameDialog = vi.inflate(R.layout.dialog_edit_label_rename_dialog, parent, false);

                                    EditText input = renameDialog.findViewById(R.id.dialog_edit_label_rename_dialog_text);
                                    input.setText(mLabels.get(position).label);

                                    new AlertDialog.Builder(mContext)
                                            .setNegativeButton("Cancel", null)
                                            .setPositiveButton("OK", (dialogInterface, which) -> {
                                                AsyncTask.execute(() ->
                                                        AppDatabase.getDatabase(mContext).labelAndColor().editLabelName(selectedLabel, input.getText().toString()));
                                                mLabels.get(position).label = input.getText().toString();
                                                if (mCurrentLabel != null && mCurrentLabel.equals(selectedLabel)) {
                                                    mCurrentLabel = input.getText().toString();
                                                }
                                                notifyDataSetChanged();
                                            })
                                            .setTitle("Rename label")
                                            .setView(renameDialog)
                                            .create().show();
                                    return true;
                                case R.id.label_row_menu_change_color:
                                    final ColorPickerDialog.Params p = new ColorPickerDialog.Params.Builder(mContext)
                                            .setColors(mContext.getResources().getIntArray(R.array.labelColors))
                                            .setSelectedColor(mLabels.get(position).color)
                                            .build();
                                    ColorPickerDialog dialog = new ColorPickerDialog(mContext, color -> {
                                        mLabels.get(position).color = color;
                                        AsyncTask.execute(() ->
                                                AppDatabase.getDatabase(mContext).labelAndColor().editLabelColor(selectedLabel, color));
                                        notifyDataSetChanged();
                                    }, p);
                                    dialog.show();

                                    return true;
                                case R.id.label_row_menu_delete:
                                    new AlertDialog.Builder(mContext)
                                            .setNegativeButton("Cancel", null)
                                            .setPositiveButton("OK", (dialogInterface, which) -> {
                                                if (selectedPosition == position) {
                                                    selectedPosition = 0;
                                                }
                                                mAdapter.remove(mLabels.get(position));
                                                AsyncTask.execute(() ->
                                                        AppDatabase.getDatabase(mContext).labelAndColor().deleteLabel(selectedLabel));
                                            })
                                            .setTitle("Delete label?")
                                            .setMessage("Are you sure you want to delete " + mLabels.get(position).label + "?")
                                            .create().show();
                                    return true;
                                default:
                                    return false;
                            }
                        });
                        popup.show();
                    });
                }

                r.setText(mLabels.get(position).label);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    r.setButtonTintList(ColorStateList.valueOf(mLabels.get(position).color));
                }
                r.setHighlightColor(mLabels.get(position).color);
                r.setTag(position);
                r.setOnClickListener(view -> {
                    clearFocusOfAddLabelView();
                    hideKeyboard(view, context);

                    selectedPosition = (Integer) view.getTag();
                    String label = mLabels.get(position).label;
                    mCurrentLabel = label.equals("unlabeled") ? null : label;
                    notifyDataSetChanged();
                });
                return v;
            }
        };

        mListView.setAdapter(mAdapter);
        mBuilder.setView(dialogLayout);
    }

    public String getLabel() {
        return mCurrentLabel;
    }

    public boolean isCurrentLabelValid() {
        return containsLabel(getLabel());
    }

    public void setPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        mBuilder.setPositiveButton("OK", listener);
    }

    public void setNegativeButtonClickListener(DialogInterface.OnClickListener listener) {
        mBuilder.setNegativeButton("Cancel", listener);
    }

    public void show() {
        mDialog = mBuilder.create();
        mDialog.show();
    }

    private int getLabelIndex(String label) {
        int currentLabelIdx = 0;
        for (int i = 0; i < mLabels.size(); ++i) {
            if (mLabels.get(i).label.equals(label)) {
                currentLabelIdx = i;
                break;
            }
        }
        return currentLabelIdx;
    }

    private boolean containsLabel(String label) {
        boolean result = false;
        for (int i = 0; i < mLabels.size(); ++i) {
            if (mLabels.get(i).label.equals(label)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void hideKeyboard(View v, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void clearFocusOfAddLabelView() {
        mAddLabelEditText.setFocusable(false);
        mAddLabelEditText.setFocusableInTouchMode(false);
        mAddLabelEditText.setText("");
        mAddLabelEditText.clearFocus();
        mDoneButton.setVisibility(View.INVISIBLE);
    }

    private void onDoneButtonClicked() {
        final String enteredString = mAddLabelEditText.getText().toString();
        if (enteredString.equals("")) {
            Toast.makeText(mContext, "Please enter a valid name", Toast.LENGTH_SHORT).show();
        } else if (EditLabelDialog.this.containsLabel(enteredString)) {
            Toast.makeText(mContext, "Label already exists", Toast.LENGTH_SHORT).show();
        } else {
            final LabelAndColor newLabel = new LabelAndColor(mAddLabelEditText.getText().toString(), mContext.getResources().getColor(R.color.white));
            if (mAdapter != null) {
                mAdapter.insert(newLabel, mAdapter.getCount());
            }
            AsyncTask.execute(() -> AppDatabase.getDatabase(mContext).labelAndColor().addLabel(newLabel));
        }
    }
}
