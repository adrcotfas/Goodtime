package com.apps.adrcotfas.goodtime.Settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.R;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import static android.graphics.Paint.UNDERLINE_TEXT_FLAG;
import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;

/**
 * Preference based on android.preference.SeekBarPreference but uses support v7 preference as base.
 * The main difference is the fact that the seekbar value is updated while the user is tracking the touch.
 * It contains a title and a seekbar and an optional seekbar value TextView. The actual preference
 * layout is customizable by setting {@code android:layout} on the preference widget layout or
 * {@code CustomSeekStyle} attribute.
 * The seekbar within the preference can be defined adjustable or not by setting {@code
 * adjustable} attribute. If adjustable, the preference will be responsive to DPAD left/right keys.
 * Otherwise, it skips those keys.
 * The seekbar value view can be shown or disabled by setting {@code showSeekBarValue} attribute
 * to true or false, respectively.
 * Other SeekBar specific attributes (e.g. {@code title, summary, defaultValue, min, max}) can be
 * set directly on the preference widget layout.
 */
public class ProperSeekBarPreference extends Preference {

    private int mSeekBarValue;
    private int mMin;
    private int mMax;
    private int mSeekBarIncrement;
    private SeekBar mSeekBar;
    private TextView mSeekBarValueTextView;
    private boolean mAdjustable; // whether the seekbar should respond to the left/right keys
    private boolean mShowSeekBarValue; // whether to show the seekbar value TextView next to the bar
    private String mUnit;
    private String mDialogTitle;

    private static final String TAG = "ProperSeekBarPreference";

    /**
     * Listener reacting to the SeekBar changing value by the user
     */
    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                syncValueInternal(seekBar);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the SeekBar
     * to be handled accordingly.
     */
    private View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (!mAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false;
            }

            // We don't want to propagate the click keys down to the seekbar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            if (mSeekBar == null) {
                Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return mSeekBar.onKeyDown(keyCode, event);
        }
    };

    public ProperSeekBarPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ProperSeekBarPreference, defStyleAttr, defStyleRes);

        /**
         * The ordering of these two statements are important. If we want to set max first, we need
         * to perform the same steps by changing min/max to max/min as following:
         * mMax = a.getInt(...) and setMin(...).
         */
        mMin = a.getInt(R.styleable.ProperSeekBarPreference_min, 0);
        setMax(a.getInt(R.styleable.ProperSeekBarPreference_android_max, 100));
        setSeekBarIncrement(a.getInt(R.styleable.ProperSeekBarPreference_seekBarIncrement, 0));
        mAdjustable = a.getBoolean(R.styleable.ProperSeekBarPreference_adjustable, true);
        mShowSeekBarValue = a.getBoolean(R.styleable.ProperSeekBarPreference_showSeekBarValue, true);
        mUnit = a.getString(R.styleable.ProperSeekBarPreference_unitOfMeasurement);
        mDialogTitle = a.getString(R.styleable.ProperSeekBarPreference_dialogTitle);
        a.recycle();
    }

    public ProperSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProperSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    public ProperSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setOnKeyListener(mSeekBarKeyListener);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        float dpi = getContext().getResources().getDisplayMetrics().density;

        mSeekBar.setPadding(0,(int)(5 * dpi),(int)(16 * dpi),(int)(5 * dpi));
        mSeekBarValueTextView = (TextView) view.findViewById(R.id.seekbar_value);

        mSeekBarValueTextView.setClickable(true);
        mSeekBarValueTextView.setPaintFlags(UNDERLINE_TEXT_FLAG);
        mSeekBarValueTextView.setTextSize(14);
        mSeekBarValueTextView.setTextColor(getContext().getResources().getColor(R.color.transparent_light));

        mSeekBarValueTextView.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            LinearLayout layout = new LinearLayout(getContext());
            final EditText input = new EditText(getContext());
            input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
            input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            input.setSingleLine();

            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(3);
            input.setFilters(FilterArray);

            layout.addView(input);
            layout.setPadding((int)(19*dpi), (int)(5*dpi), (int)(19*dpi), (int)(5*dpi));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            builder.setTitle(mDialogTitle)
                    .setView(layout);
            builder.setPositiveButton("OK", (di, i) -> {
                final String name = input.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    setValueInternal(Integer.parseInt(name), true);
                }
            });
            builder.setNegativeButton("Cancel", (di, i) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
        });

        if (mShowSeekBarValue) {
            mSeekBarValueTextView.setVisibility(View.VISIBLE);
        } else {
            mSeekBarValueTextView.setVisibility(View.GONE);
            mSeekBarValueTextView = null;
        }

        if (mSeekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBar.setMax(mMax - mMin);
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (mSeekBarIncrement != 0) {
            mSeekBar.setKeyProgressIncrement(mSeekBarIncrement);
        } else {
            mSeekBarIncrement = mSeekBar.getKeyProgressIncrement();
        }

        mSeekBar.setProgress(mSeekBarValue - mMin);
        if (mSeekBarValueTextView != null) {
            mSeekBarValueTextView.setText(String.valueOf(mSeekBarValue) + mUnit);
        }
        mSeekBar.setEnabled(isEnabled());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mSeekBarValue)
                : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public void setMin(int min) {
        if (min > mMax) {
            min = mMax;
        }
        if (min != mMin) {
            mMin = min;
            notifyChanged();
        }
    }

    public int getMin() {
        return mMin;
    }

    public final void setMax(int max) {
        if (max < mMin) {
            max = mMin;
        }
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     * @return The amount of increment on the SeekBar performed after each user's arrow key press.
     */
    public final int getSeekBarIncrement() {
        return mSeekBarIncrement;
    }

    /**
     * Sets the increment amount on the SeekBar for each arrow key press.
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     *                         arrow key.
     */
    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != mSeekBarIncrement) {
            mSeekBarIncrement =  Math.min(mMax - mMin, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    public int getMax() {
        return mMax;
    }

    public void setAdjustable(boolean adjustable) {
        mAdjustable = adjustable;
    }

    public boolean isAdjustable() {
        return mAdjustable;
    }

    public void setValue(int seekBarValue) {
        setValueInternal(seekBarValue, true);
    }

    private void setValueInternal(int seekBarValue, boolean notifyChanged) {
        if (seekBarValue < mMin) {
            seekBarValue = mMin;
        }
        if (seekBarValue > mMax) {
            seekBarValue = mMax;
        }

        if (seekBarValue != mSeekBarValue) {
            mSeekBarValue = seekBarValue;
            if (mSeekBarValueTextView != null) {
                mSeekBarValueTextView.setText(String.valueOf(mSeekBarValue) + mUnit);
            }
            persistInt(seekBarValue);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getValue() {
        return mSeekBarValue;
    }

    /**
     * Persist the seekBar's seekbar value if callChangeListener
     * returns true, otherwise set the seekBar's value to the stored value
     */
    private void syncValueInternal(SeekBar seekBar) {
        int seekBarValue = mMin + seekBar.getProgress();
        if (seekBarValue != mSeekBarValue) {
            if (callChangeListener(seekBarValue)) {
                setValueInternal(seekBarValue, false);
            } else {
                seekBar.setProgress(mSeekBarValue - mMin);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.seekBarValue = mSeekBarValue;
        myState.min = mMin;
        myState.max = mMax;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mSeekBarValue = myState.seekBarValue;
        mMin = myState.min;
        mMax = myState.max;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        int seekBarValue;
        int min;
        int max;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            seekBarValue = source.readInt();
            min = source.readInt();
            max = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(seekBarValue);
            dest.writeInt(min);
            dest.writeInt(max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
