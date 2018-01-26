package com.pavelsikun.seekbarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * Created by Pavel Sikun on 28.05.16.
 */

@SuppressWarnings("WeakerAccess")
class PreferenceControllerDelegate implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
	private static final boolean DEBUG = true && BuildConfig.DEBUG;
	public static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";
	private final String TAG = getClass().getSimpleName();

	private static final int DEFAULT_CURRENT_VALUE = 50;
	private static final int DEFAULT_MIN_VALUE = 0;
	private static final int DEFAULT_MAX_VALUE = 100;
	private static final int DEFAULT_INTERVAL = 1;
	private static final boolean DEFAULT_DIALOG_ENABLED = true;
	private static final boolean DEFAULT_IS_ENABLED = true;

	private static final int DEFAULT_DIALOG_STYLE = R.style.MSB_Dialog_Default;
	private static final Pattern FORMAT_PATTERN = Pattern.compile(".*(?:[^%]%)[dhs].*");
	private int maxValue;
	private int seekMaxValue;
	private int minValue;
	private int interval;
	private int currentValue;
	private String unit;
	private boolean unitIsFormat;
	@PluralsRes
	private int summaryPluralResId;
	@PluralsRes
	private int unitPluralsResId;
	private boolean dialogEnabled;

	private int dialogStyle;

	private TextView valueView;
	private SeekBar seekBarView;
	private LinearLayout valueHolderView;
	private FrameLayout bottomLineView;

	//view stuff
	private TextView titleView, summaryView;
	private String title;
	private String summary;
	private boolean isEnabled;

	//controller stuff
	private boolean isView = false;
	private Context context;
	private ViewStateListener viewStateListener;
	private PersistValueListener persistValueListener;
	private ChangeValueListener changeValueListener;

	interface ViewStateListener {
		boolean isEnabled();

		void setEnabled(boolean enabled);
	}

	PreferenceControllerDelegate(Context context, Boolean isView) {
		this.context = context;
		this.isView = isView;
	}

	void setPersistValueListener(PersistValueListener persistValueListener) {
		this.persistValueListener = persistValueListener;
	}

	void setViewStateListener(ViewStateListener viewStateListener) {
		this.viewStateListener = viewStateListener;
	}

	void setChangeValueListener(ChangeValueListener changeValueListener) {
		this.changeValueListener = changeValueListener;
	}

	void loadValuesFromXml(AttributeSet attrs) {
		if (attrs == null) {
			currentValue = DEFAULT_CURRENT_VALUE;
			minValue = DEFAULT_MIN_VALUE;
			maxValue = DEFAULT_MAX_VALUE;
			interval = DEFAULT_INTERVAL;
			dialogEnabled = DEFAULT_DIALOG_ENABLED;
			summaryPluralResId = 0;
			unitPluralsResId = 0;

			isEnabled = DEFAULT_IS_ENABLED;
		} else {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
			try {
				minValue = a.getInt(R.styleable.SeekBarPreference_msbp_minValue, DEFAULT_MIN_VALUE);
				maxValue = a.getInt(R.styleable.SeekBarPreference_msbp_maxValue, DEFAULT_MAX_VALUE);
				interval = a.getInt(R.styleable.SeekBarPreference_msbp_interval, DEFAULT_INTERVAL);
				seekMaxValue = (maxValue - minValue) / interval;

				dialogEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_dialogEnabled, DEFAULT_DIALOG_ENABLED);

				// plurals support for units
				unitPluralsResId = a.getResourceId(R.styleable.SeekBarPreference_msbp_measurementUnit, 0);
				if (unitPluralsResId != 0) {
					String unitPluralResType = context.getResources().getResourceTypeName(unitPluralsResId);
					if (unitPluralResType.equalsIgnoreCase("plurals")) {
						unit = null;
						unitIsFormat = true;
					} else {
						// not a plural, string, probably, won't check.
						unitPluralsResId = 0;
					}
				}

				if (unitPluralsResId == 0) {
					setUnit(a.getString(R.styleable.SeekBarPreference_msbp_measurementUnit));
					unitIsFormat = isFormatString(unit);
				}

				currentValue = attrs.getAttributeIntValue(NS_ANDROID, "defaultValue", DEFAULT_CURRENT_VALUE);

				dialogStyle = a.getResourceId(R.styleable.SeekBarPreference_msbp_dialogStyle, DEFAULT_DIALOG_STYLE);

				if (isView) {
					title = a.getString(R.styleable.SeekBarPreference_msbp_view_title);

					currentValue = a.getInt(R.styleable.SeekBarPreference_msbp_view_defaultValue, DEFAULT_CURRENT_VALUE);

					isEnabled = a.getBoolean(R.styleable.SeekBarPreference_msbp_view_enabled, DEFAULT_IS_ENABLED);

					// following lines are dealing with plurals resource for summary
					// plurals resource may be specified in "msbp_view_summary"
					// or "android:summary" (takes precedence)

					// try "android:summary" for reference first
					int id = attrs.getAttributeResourceValue(NS_ANDROID, "summary", 0);
					String summary = null;
					if (id == 0) { // didn't work
						// try "msbp_view_summary" then
						id = a.getResourceId(R.styleable.SeekBarPreference_msbp_view_summary, 0);
						if (id == 0) {// no reference supplied
							// try "msbp_view_summary" for string then
							summary = a.getString(R.styleable.SeekBarPreference_msbp_view_summary);
							if (TextUtils.isEmpty(summary)) { //didn't work
								// try "android:summary" for string then
								summary = attrs.getAttributeValue(NS_ANDROID, "summary");
							}
						}
					}
					// prob. modified value, another branch, but not "else"!
					if (id != 0) {
						// ok, we've got a reference
						summary = null;
						String summaryResType = context.getResources().getResourceTypeName(id);
						if (!summaryResType.equalsIgnoreCase("plurals")) {
							// but it's not plural resource!
							if (summaryResType.equalsIgnoreCase("string"))// ok, it's string
								summary = context.getResources().getString(id);
							// otherwise -- stick to summary = null and summaryResId = 0.
						}
					}
					summaryPluralResId = TextUtils.isEmpty(summary) ? id : 0;
					this.summary = summary;
				}
			}
			finally {
				a.recycle();
			}
		}
	}

	private boolean isFormatString(String s) {
		boolean result = (s != null) && FORMAT_PATTERN.matcher(s).matches();
		if (DEBUG) Log.d(TAG, '\"' + s + "\" is" + (result ? "" : " not") + " a format string, for sure.");
		return result;
	}


	void onBind(View view) {

		if (isView) {
			titleView = view.findViewById(android.R.id.title);
			summaryView = view.findViewById(android.R.id.summary);

			titleView.setText(title);
			summaryView.setText(summary);
		}

		view.setClickable(false);

		seekBarView = view.findViewById(R.id.seekbar);
		valueView = view.findViewById(R.id.seekbar_value);

		setMaxValue(maxValue);
		seekBarView.setOnSeekBarChangeListener(this);

		setCurrentValue(currentValue);
		bindCurrentValueToView();

		bottomLineView = view.findViewById(R.id.bottom_line);
		valueHolderView = view.findViewById(R.id.value_holder);

		setDialogEnabled(dialogEnabled);
		setEnabled(isEnabled(), true);
	}

	private void bindCurrentValueToView() {
		CharSequence text;
		if (unitIsFormat) {
			if (isUsingPluralsForUnits()) {
				text = context.getResources().getQuantityString(unitPluralsResId, currentValue, currentValue);
			} else text = String.format(unit, currentValue);
		} else if (!TextUtils.isEmpty(unit))
			text = currentValue + unit;
		else text = Integer.toString(currentValue);
		valueView.setText(text);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int newValue = progressToValue(progress);

		if (changeValueListener != null) {
			if (!changeValueListener.onChange(newValue)) {
				return;
			}
		}
		bindValue(newValue);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		setCurrentValue(currentValue);
		bindCurrentValueToView();
	}

	@Override
	public void onClick(final View v) {
		new CustomValueDialog(context, dialogStyle, minValue, maxValue, currentValue)
				.setPersistValueListener(new PersistValueListener() {
					@Override
					public boolean persistInt(int value) {
						setCurrentValue(value);
						seekBarView.setOnSeekBarChangeListener(null);
						seekBarView.setProgress(valueToProgress(currentValue));
						seekBarView.setOnSeekBarChangeListener(PreferenceControllerDelegate.this);

						bindCurrentValueToView();
						return true;
					}
				})
				.show();
	}


	String getTitle() {
		return title;
	}

	void setTitle(String title) {
		this.title = title;
		if (titleView != null) {
			titleView.setText(title);
		}
	}

	String getSummary() {
		return summary;
	}

	void setSummary(String summary) {
		this.summary = summary;
		if (seekBarView != null) {
			summaryView.setText(summary);
		}
	}

	boolean isEnabled() {
		if (!isView && viewStateListener != null) {
			return viewStateListener.isEnabled();
		} else return isEnabled;
	}

	void setEnabled(boolean enabled, boolean viewsOnly) {
		if (DEBUG) Log.d(TAG, "setEnabled = " + enabled);
		isEnabled = enabled;

		if (viewStateListener != null && !viewsOnly) {
			viewStateListener.setEnabled(enabled);
		}

		if (seekBarView != null) { //theoretically might not always work
			if (DEBUG) Log.d(TAG, "view is disabled!");
			seekBarView.setEnabled(enabled);
			valueView.setEnabled(enabled);
			valueHolderView.setClickable(enabled);
			valueHolderView.setEnabled(enabled);

			bottomLineView.setEnabled(enabled);

			if (isView) {
				titleView.setEnabled(enabled);
				summaryView.setEnabled(enabled);
			}
		}

	}

	void setEnabled(boolean enabled) {
		setEnabled(enabled, false);
	}

	int getMaxValue() {
		return maxValue;
	}

	void setMaxValue(int maxValue) {
		this.maxValue = maxValue;

		if (seekBarView != null) {
			/*if (minValue <= 0 && maxValue >= 0) {
			} else {
				seekBarView.setMax(maxValue / interval);
			}*/
			seekMaxValue = valueToProgress(maxValue);
			seekBarView.setMax(seekMaxValue);

			seekBarView.setProgress(valueToProgress(currentValue));
		}
	}

	private int valueToProgress(int value) {
		if (value >= maxValue) return seekMaxValue;
		if (value <= minValue) return 0;
		return (value - minValue) / interval;
	}

	private int progressToValue(int progress) {
		if (progress >= seekMaxValue) return maxValue;
		if (progress <= 0) return minValue;
		return progress * interval + minValue;
	}

	int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
		setMaxValue(maxValue);
	}

	int getInterval() {
		return interval;
	}

	void setInterval(int interval) {
		this.interval = interval;
	}

	int getCurrentValue() {
		return currentValue;
	}

	private void bindValue(int value) {
		setCurrentValue(value);
		bindCurrentValueToView();
	}

	void setCurrentValue(int value) {
		if (value < minValue) value = minValue;
		if (value > maxValue) value = maxValue;

		if (changeValueListener != null) {
			if (!changeValueListener.onChange(value)) {
				return;
			}
		}
		currentValue = value;
		if (seekBarView != null)
			seekBarView.setProgress(valueToProgress(currentValue));

		if (persistValueListener != null) {
			persistValueListener.persistInt(value);
		}
	}

	public boolean isUsingPluralsForUnits() {
		return unitPluralsResId != 0;
	}

	@Nullable
	String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		if (Character.isWhitespace(unit.charAt(0)))
			this.unit = unit;
		else
			this.unit = ' ' + unit;

		unitIsFormat = isFormatString(unit);
	}

	public boolean isUnitAFormat() {
		return unitIsFormat;
	}

	@PluralsRes
	int getUnitPluralsId() {
		return unitPluralsResId;
	}

	/**
	 * @param pluralsId id of resource that specifies plurals for this unit
	 */
	void setUnitPlurals(@PluralsRes int pluralsId) {
		if (!context.getResources().getResourceTypeName(pluralsId).equalsIgnoreCase("plurals"))
			throw new IllegalArgumentException("Specified resource is not plural!");

		unitPluralsResId = pluralsId;
		bindCurrentValueToView();
	}

	boolean isDialogEnabled() {
		return dialogEnabled;
	}

	void setDialogEnabled(boolean dialogEnabled) {
		this.dialogEnabled = dialogEnabled;

		if (valueHolderView != null && bottomLineView != null) {
			valueHolderView.setOnClickListener(dialogEnabled ? this : null);
			valueHolderView.setClickable(dialogEnabled);
			bottomLineView.setVisibility(dialogEnabled ? View.VISIBLE : View.INVISIBLE);
		}
	}

	void setDialogStyle(int dialogStyle) {
		this.dialogStyle = dialogStyle;
	}
}
