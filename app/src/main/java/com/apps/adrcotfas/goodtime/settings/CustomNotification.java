package com.apps.adrcotfas.goodtime.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import com.apps.adrcotfas.goodtime.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static com.apps.adrcotfas.goodtime.Preferences.NOTIFICATION_SOUND;

public class CustomNotification {
    public static final String PREF_KEY_RINGTONE_DEFAULT = "pref_key_ringtone_default";
    public static final String PREF_KEY_RINGTONES_COPIED = "pref_key_ringtones_copied";

    private static final String tag = "CustomNotification";

    private static Context mContext;

    /**
     * Copies the alarms to shared storage in a separate thread
     * @param context The context to use to retrieve the resources
     */
    public static void installToStorage(Context context) {
        mContext = context;
        new Thread(runnable).start();
    }

    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mContext == null) {
                return;
            }

            boolean status = copyRawFile(R.raw.goodtime_notif,
                    mContext.getString(R.string.goodtime_notification_title), true);

            // If successful, record to SharedPreferences
            if (status) {
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(mContext);
                sharedPrefs.edit().putBoolean(PREF_KEY_RINGTONES_COPIED, true).apply();
            }
            mContext = null;
        }

        /**
         * Copies a raw resource into the alarms directory on the device's shared storage
         * @param resID The resource ID of the raw resource to copy, in the form of R.raw.*
         * @param title The title to use for the alarm tone
         * @param setAsDefault Set the file as the default alarm tone for the app
         * @return Whether the file was copied successfully
         */
        private boolean copyRawFile(int resID, String title, boolean setAsDefault) {

            // Make sure the shared storage is currently writable
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                return false;

            File path = Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS);
            path.mkdirs();
            String filename = mContext.getResources().getResourceEntryName(resID) + ".mp3";
            File outFile = new File(path, filename);

            String mimeType = "audio/mpeg";

            boolean isError = false;

            // Write the file
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = mContext.getResources().openRawResource(resID);
                outputStream = new FileOutputStream(outFile);

                // Write in 1024-byte chunks
                byte[] buffer = new byte[1024];
                int bytesRead;
                // Keep writing until `inputStream.read()` returns -1, which means we reached the
                //  end of the stream
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Set the file metadata
                String outAbsPath = outFile.getAbsolutePath();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DATA, outAbsPath);
                contentValues.put(MediaStore.MediaColumns.TITLE, title);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                contentValues.put(MediaStore.Audio.Media.IS_ALARM, false);
                contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                contentValues.put(MediaStore.Audio.Media.IS_MUSIC, false);

                Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(outAbsPath);

                // If the ringtone already exists in the database, delete it first
                mContext.getContentResolver().delete(contentUri,
                        MediaStore.MediaColumns.DATA + "=\"" + outAbsPath + "\"", null);

                // Add the metadata to the file in the database
                Uri newUri = mContext.getContentResolver().insert(contentUri, contentValues);

                // Tell the media scanner about the new ringtone
                MediaScannerConnection.scanFile(
                        mContext,
                        new String[]{newUri.toString()},
                        new String[]{mimeType},
                        null
                );

                if (setAsDefault) {
                    SharedPreferences sharedPrefs =
                            PreferenceManager.getDefaultSharedPreferences(mContext);

                    // Save this to SharedPreferences, so SettingsFragment can use it as the default
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(PREF_KEY_RINGTONE_DEFAULT, newUri.toString());

                    // Check if the ringtone preference is currently set to "default"
                    if (sharedPrefs.getString(NOTIFICATION_SOUND, "")
                            .equals(mContext.getString(R.string.default_ringtone_path))) {
                        // Set this as the ringtone, since it's the new default
                        editor.putString(NOTIFICATION_SOUND, newUri.toString());
                    }

                    editor.apply();
                }

                Log.d(tag, "Copied notification sound " + title + " to " + outAbsPath);
                Log.d(tag, "ID is " + newUri.toString());

            } catch (Exception e) {
                Log.e(tag, "Error writing " + filename, e);
                isError = true;
            } finally {
                // Close the streams
                try {
                    if (inputStream != null)
                        inputStream.close();
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    // Means there was an error trying to close the streams, so do nothing
                }
            }
            return !isError;
        }

    };

}