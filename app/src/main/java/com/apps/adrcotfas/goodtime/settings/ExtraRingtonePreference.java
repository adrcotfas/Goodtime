package com.apps.adrcotfas.goodtime.settings;
import com.apps.adrcotfas.goodtime.R;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ExtraRingtonePreference extends DialogPreference {

    private Context mContext;
    private String mValue;
    private Ringtone ringtone;
    private int mRingtoneType;
    private boolean mShowSilent;
    private boolean mShowDefault;
    private CharSequence[] mExtraRingtones;
    private CharSequence[] mExtraRingtoneTitles;

    public ExtraRingtonePreference(Context context, AttributeSet attrs) {

        super(context, attrs);

        mContext = context;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtraRingtonePreference, 0, 0);

        mRingtoneType = a.getInt(R.styleable.ExtraRingtonePreference_ringtoneType, RingtoneManager.TYPE_RINGTONE);
        mShowDefault = a.getBoolean(R.styleable.ExtraRingtonePreference_showDefault, true);
        mShowSilent = a.getBoolean(R.styleable.ExtraRingtonePreference_showSilent, true);
        mExtraRingtones = a.getTextArray(R.styleable.ExtraRingtonePreference_extraRingtones);
        mExtraRingtoneTitles = a.getTextArray(R.styleable.ExtraRingtonePreference_extraRingtoneTitles);

        a.recycle();
    }

    public ExtraRingtonePreference(Context context) {
        this(context, null);
    }

    public String getValue() {
        return mValue;
    }

    private Map<String, Uri> getSounds(int type) {

        RingtoneManager ringtoneManager = new RingtoneManager(mContext);
        ringtoneManager.setType(type);
        Cursor cursor = ringtoneManager.getCursor();

        Map<String, Uri> list = new TreeMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri notificationUri =  ringtoneManager.getRingtoneUri(cursor.getPosition());

            list.put(notificationTitle, notificationUri);
        }

        return list;
    }

    private Uri uriFromRaw(String name) {
        int resId = mContext.getResources().getIdentifier(name, "raw", mContext.getPackageName());
        return Uri.parse("android.resource://" + mContext.getPackageName() + "/" + resId);
    }

    private String getExtraRingtoneTitle(CharSequence name) {
        if (mExtraRingtones != null && mExtraRingtoneTitles != null) {
            int index = Arrays.asList(mExtraRingtones).indexOf(name);
            return mExtraRingtoneTitles[index].toString();
        }

        return null;
    }

    @Override
    public CharSequence getSummary() {

        String ringtoneTitle = null;

        if (mValue != null) {

            if (mValue.length() == 0)
                ringtoneTitle = mContext.getString(R.string.silent);

            if (ringtoneTitle == null && mExtraRingtones != null && mExtraRingtoneTitles != null) {

                for (int i = 0; i < mExtraRingtones.length; i++) {

                    Uri uriExtra = uriFromRaw(mExtraRingtones[i].toString());

                    if (uriExtra.equals(Uri.parse(mValue))) {
                        ringtoneTitle = mExtraRingtoneTitles[i].toString();
                        break;
                    }
                }
            }

            if (ringtoneTitle == null) {
                Ringtone ringtone = RingtoneManager.getRingtone(mContext, Uri.parse(mValue));
                String title = ringtone.getTitle(mContext);
                if (title != null && title.length() > 0)
                    ringtoneTitle = title;
            }

        }

        CharSequence summary = super.getSummary();

        if (ringtoneTitle != null) {
            if (summary != null)
                return String.format(summary.toString(), ringtoneTitle);
            else
                return ringtoneTitle;
        } else return summary;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {

        final Map<String, Uri> sounds = new LinkedHashMap<>();

        if (mExtraRingtones != null) {
            for (CharSequence extraRingtone : mExtraRingtones) {
                Uri uri = uriFromRaw(extraRingtone.toString());
                String title = getExtraRingtoneTitle(extraRingtone);

                sounds.put(title, uri);
            }
        }

        if (mShowDefault) {
            Uri uriDefault = RingtoneManager.getDefaultUri(mRingtoneType);
            if (uriDefault != null) {
                Ringtone ringtoneDefault = RingtoneManager.getRingtone(mContext, uriDefault);
                if (ringtoneDefault != null) {
                    sounds.put(ringtoneDefault.getTitle(mContext), uriDefault);
                }
            }
        }

        if (mShowSilent)
            sounds.put(mContext.getString(R.string.silent), Uri.parse(""));


        sounds.putAll(getSounds(RingtoneManager.TYPE_NOTIFICATION));


        final String[] titleArray = sounds.keySet().toArray(new String[0]);
        final Uri[] uriArray = sounds.values().toArray(new Uri[0]);

        int index = mValue != null ? Arrays.asList(uriArray).indexOf(Uri.parse(mValue)) : -1;

        builder.setSingleChoiceItems(titleArray, index, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (ringtone != null)
                    ringtone.stop();

                String title = titleArray[which];
                Uri uri = uriArray[which];

                if (uri != null) {
                    if (uri.toString().length() > 0) {
                        ringtone = RingtoneManager.getRingtone(mContext, uri);
                        ringtone.play();
                    }
                    mValue = uri.toString();
                } else mValue = null;

            }
        });

        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        super.onDialogClosed(positiveResult);

        if (ringtone != null)
            ringtone.stop();

        if (positiveResult && callChangeListener(mValue)) {
            persistString(mValue);
            notifyChanged();
        }

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue)
            mValue = getPersistedString("");
        else {
            if (mExtraRingtones != null && defaultValue != null && defaultValue.toString().length() > 0) {

                int index = Arrays.asList(mExtraRingtones).indexOf((CharSequence) defaultValue);
                if (index >= 0)
                    mValue = uriFromRaw(defaultValue.toString()).toString();
                else mValue = (String) defaultValue;

            } else mValue = (String) defaultValue;

            persistString(mValue);
        }


    }


}