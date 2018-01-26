package com.pavelsikun.seekbarpreference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Pavel Sikun on 21.05.16.
 */
class CustomValueDialog {

    private final String TAG = getClass().getSimpleName();

    private static final int DEFAULT_CANCEL_RES_ID = android.R.string.cancel;
    private static final int DEFAULT_OK_RES_ID = android.R.string.ok;
    private static final int DEFAULT_TITLE_RES_ID = R.string.enter_custom_value;

    private Dialog dialog;
    private EditText customValueView;

    private int minValue, maxValue, currentValue;
    private PersistValueListener persistValueListener;

    private String okText = null;
    private String cancelText = null;
    private String titleText = null;

    CustomValueDialog(Context context, int theme, int minValue, int maxValue, int currentValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;

        if (theme != 0) {
            TypedArray a = null;
            try {
                a = context.obtainStyledAttributes(theme, R.styleable.SeekBarPreference);
                int cancelId = a.getResourceId(R.styleable.SeekBarPreference_msbp_dialogCancel, 0);
                if (cancelId == 0) {
                    cancelText = a.getString(R.styleable.SeekBarPreference_msbp_dialogCancel);
                    if (cancelText == null)
                        cancelText = context.getString(DEFAULT_CANCEL_RES_ID);
                } else
                    cancelText = context.getString(cancelId);

                int okId = a.getResourceId(R.styleable.SeekBarPreference_msbp_dialogOk, 0);
                if (okId == 0) {
                    okText = a.getString(R.styleable.SeekBarPreference_msbp_dialogOk);
                    if (okText == null)
                        okText = context.getString(DEFAULT_OK_RES_ID);
                } else
                    okText = context.getString(okId);

                int titleId = a.getResourceId(R.styleable.SeekBarPreference_msbp_dialogTitle, 0);
                if (titleId == 0) {
                    titleText = a.getString(R.styleable.SeekBarPreference_msbp_dialogTitle);
                    if (titleText == null)
                        titleText = context.getString(DEFAULT_TITLE_RES_ID);
                } else
                    titleText = context.getString(titleId);

            } finally {
                if (a != null) a.recycle();
            }

        }
        init(new AlertDialog.Builder(context, theme));
    }

    private void init(AlertDialog.Builder dialogBuilder) {
        View dialogView = LayoutInflater.from(dialogBuilder.getContext()).inflate(R.layout.value_selector_dialog, null);

        TextView minValueView = dialogView.findViewById(R.id.minValue);
        TextView maxValueView = dialogView.findViewById(R.id.maxValue);
        customValueView = dialogView.findViewById(R.id.customValue);

        minValueView.setText(String.valueOf(minValue));
        maxValueView.setText(String.valueOf(maxValue));
        customValueView.setHint(String.valueOf(currentValue));

        if (!TextUtils.isEmpty(titleText)) {
            TextView titleView = dialogView.findViewById(R.id.dialog_title);
            if (titleView != null)
                titleView.setText(titleText);
        }

        LinearLayout colorView = dialogView.findViewById(R.id.dialog_color_area);
        colorView.setBackgroundColor(fetchAccentColor(dialogBuilder.getContext()));

        if (!TextUtils.isEmpty(okText))
            dialogBuilder.setPositiveButton(okText, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tryApply();
                }
            });

        if (!TextUtils.isEmpty(cancelText))
            dialogBuilder.setNegativeButton(cancelText, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        dialog = dialogBuilder.setView(dialogView).create();
    }

    private int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();

        return color;
    }

    CustomValueDialog setPersistValueListener(PersistValueListener listener) {
        persistValueListener = listener;
        return this;
    }

    void show() {
        dialog.show();
    }

    private void tryApply() {
        int value;

        try {
            value = Integer.parseInt(customValueView.getText().toString());
            if (value > maxValue) {
                Log.e(TAG, "wrong input( > than required): " + customValueView.getText().toString());
                notifyWrongInput();
                return;
            } else if (value < minValue) {
                Log.e(TAG, "wrong input( < then required): " + customValueView.getText().toString());
                notifyWrongInput();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "worng input(non-integer): " + customValueView.getText().toString());
            notifyWrongInput();
            return;
        }

        if (persistValueListener != null) {
            persistValueListener.persistInt(value);
            dialog.dismiss();
        }
    }

    private void notifyWrongInput() {
        customValueView.setText("");
        customValueView.setHint("Wrong Input!");
    }
}
