package com.apps.adrcotfas.goodtime.BL;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

import java.io.IOException;

public class RingtoneAndVibrationPlayer extends ContextWrapper{

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    public RingtoneAndVibrationPlayer(Context context) {
        super(context);
    }

    public void play(SessionType sessionType) {
        try {
            mMediaPlayer = new MediaPlayer();
            mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

            final Uri uri = Uri.parse( sessionType == SessionType.WORK ?
                    PreferenceHelper.getNotificationSound() : PreferenceHelper.getNotificationSoundBreak());

            mMediaPlayer.setDataSource(this, uri);
            if (PreferenceHelper.isRingtoneEnabled()) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(PreferenceHelper.isRingtoneInsistent());
                mMediaPlayer.prepareAsync();
            }

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });

            if (PreferenceHelper.isVibrationEnabled()) {
                mVibrator.vibrate(new long[] {0, 500, 500, 500},
                        PreferenceHelper.isRingtoneInsistent() ? 2 : -1);
            }
        } catch (SecurityException | IOException e) {
            stop();
        }
    }

    public void stop() {
        if (mMediaPlayer != null && mVibrator != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

}
