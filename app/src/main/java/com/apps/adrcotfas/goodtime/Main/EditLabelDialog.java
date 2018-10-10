package com.apps.adrcotfas.goodtime.Main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;

import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class EditLabelDialog {

    private Context mContext;
    private String mCurrentLabel;
    private AlertDialog mDialog;
    private AlertDialog.Builder mBuilder;

    public EditLabelDialog(Context context, List<LabelAndColor> labels, String crtLabel) {
        mContext = context;
        mCurrentLabel = crtLabel;
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle("Select label");
        mBuilder.setPositiveButton("OK", null);
        mBuilder.setNegativeButton("Cancel", (dialogInterface, which) -> mDialog.dismiss());
        mBuilder.setNeutralButton("Edit labels", (dialogInterface, which) -> {
            AsyncTask.execute(() -> {
                //TODO: replace with dialog or fragment to add and edit labels
                AppDatabase.getDatabase(mContext).labelAndColor()
                        .addLabel(new LabelAndColor("art", mContext.getResources().getColor(R.color.pref_blue)));
                AppDatabase.getDatabase(mContext).labelAndColor()
                        .addLabel(new LabelAndColor("engineering", mContext.getResources().getColor(R.color.pref_red)));
            });
            mDialog.dismiss();
        });

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogLayout = inflater.inflate(R.layout.dialog_edit_label, null);

        ListView listView = dialogLayout.findViewById(R.id.label_list);
        ArrayAdapter<LabelAndColor> adapter = new ArrayAdapter<LabelAndColor>(mContext, R.layout.dialog_edit_label_row, R.id.radio_button, labels) {

            int selectedPosition = getLabelIndex(labels, crtLabel);

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.dialog_edit_label_row, null);
                }
                RadioButton r = v.findViewById(R.id.radio_button);
                r.setChecked(position == selectedPosition);
                r.setText(labels.get(position).label);
                r.setButtonTintList(ColorStateList.valueOf(labels.get(position).color));
                r.setHighlightColor(labels.get(position).color);
                r.setTag(position);
                r.setOnClickListener(view -> {
                    selectedPosition = (Integer) view.getTag();
                    mCurrentLabel = (labels.get(position).label);
                    notifyDataSetChanged();
                });
                return v;
            }
        };
        listView.setAdapter(adapter);
        mBuilder.setView(dialogLayout);
    }

    public String getLabel() {
        return mCurrentLabel;
    }

    public void setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        mBuilder.setPositiveButton("OK", listener);
    }

    public void show() {
        mDialog = mBuilder.create();
        mDialog.show();
    }

    private int getLabelIndex(List<LabelAndColor> labels, String label) {
        int currentLabelIdx = 0;
        for (int i = 0; i < labels.size(); ++i) {
            if (labels.get(i).label.equals(label)) {
                currentLabelIdx = i;
                break;
            }
        }
        return currentLabelIdx;
    }
}
