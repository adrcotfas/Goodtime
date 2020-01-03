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

package com.apps.adrcotfas.goodtime.BL;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import java.io.IOException;

class RingtoneAndVibrationPlayer extends ContextWrapper{

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    public RingtoneAndVibrationPlayer(Context context) {
        super(context);
    }

    public void play(SessionType sessionType) {
        try {
            mMediaPlayer = new MediaPlayer();
            mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

            final String ringtone = sessionType == SessionType.WORK ?
                    PreferenceHelper.getNotificationSoundWorkFinished() : PreferenceHelper.getNotificationSoundBreakFinished();
            Uri uri;
            if (ringtone.equals("")) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            } else {
                uri = Uri.parse(ringtone);
            }

            mMediaPlayer.setDataSource(this, uri);
            if (PreferenceHelper.isRingtoneEnabled()) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(PreferenceHelper.isRingtoneInsistent());
                mMediaPlayer.prepareAsync();
            }

            mMediaPlayer.setOnPreparedListener(mp -> {
                // TODO: check duration of custom ringtones which may be much longer than notification sounds.
                // If it's n seconds long and we're in continuous mode,
                // schedule a stop after x seconds.
                mMediaPlayer.start();
            });

            switch (PreferenceHelper.getVibrationType()) {
                case STRONG:
                    mVibrator.vibrate(new long[] {0, 500, 500, 500},
                            PreferenceHelper.isRingtoneInsistent() ? 2 : -1);
                    break;

                case SOFT:
                    mVibrator.vibrate(new long[] {0, 50, 50, 50, 50, 50},
                            PreferenceHelper.isRingtoneInsistent() ? 2 : -1);
                    break;

                default:
                    // Either "NONE" or an invalid value: no vibration
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
