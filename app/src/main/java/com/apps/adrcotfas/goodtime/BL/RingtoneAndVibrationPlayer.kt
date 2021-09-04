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
package com.apps.adrcotfas.goodtime.BL

import android.content.Context
import android.content.ContextWrapper
import android.media.MediaPlayer
import android.os.Vibrator
import android.media.AudioManager
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper
import android.net.Uri
import com.apps.adrcotfas.goodtime.Settings.toRingtone
import com.apps.adrcotfas.goodtime.Util.VibrationPatterns
import java.io.IOException

internal class RingtoneAndVibrationPlayer(context: Context?) : ContextWrapper(context) {
    private var mMediaPlayer: MediaPlayer? = null
    private var mVibrator: Vibrator? = null
    private val mAudioManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    fun play(sessionType: SessionType, insistent: Boolean) {
        try {
            mVibrator = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (PreferenceHelper.isRingtoneEnabled()) {
                val ringtoneRaw =
                    if (sessionType == SessionType.WORK) PreferenceHelper.getNotificationSoundWorkFinished()
                    else PreferenceHelper.getNotificationSoundBreakFinished()

                val uri = Uri.parse(toRingtone(ringtoneRaw).uri)

                mMediaPlayer = MediaPlayer()
                mMediaPlayer!!.setDataSource(this, uri)
                mAudioManager.mode = AudioManager.MODE_NORMAL
                mMediaPlayer!!.setAudioStreamType(if (PreferenceHelper.isPriorityAlarm()) AudioManager.STREAM_ALARM else AudioManager.STREAM_NOTIFICATION)
                mMediaPlayer!!.isLooping = insistent
                mMediaPlayer!!.prepareAsync()
                mMediaPlayer!!.setOnPreparedListener { mp: MediaPlayer? ->
                    // TODO: check duration of custom ringtones which may be much longer than notification sounds.
                    // If it's n seconds long and we're in continuous mode,
                    // schedule a stop after x seconds.
                    mMediaPlayer!!.start()
                }
            }
            val vibrationType = PreferenceHelper.getVibrationType()
            if (vibrationType > 0) {
                mVibrator!!.vibrate(
                    VibrationPatterns.LIST[vibrationType],
                    if (insistent) 2 else -1
                )
            }
        } catch (e: SecurityException) {
            stop()
        } catch (e: IOException) {
            stop()
        }
    }

    fun stop() {
        if (mMediaPlayer != null && mVibrator != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        if (mVibrator != null) {
            mVibrator!!.cancel()
        }
    }

}