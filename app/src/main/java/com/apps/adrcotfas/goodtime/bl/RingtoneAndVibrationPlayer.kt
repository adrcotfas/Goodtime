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
package com.apps.adrcotfas.goodtime.bl

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Vibrator
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.settings.toRingtone
import com.apps.adrcotfas.goodtime.util.VibrationPatterns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class RingtoneAndVibrationPlayer @Inject constructor(
    @ApplicationContext val context: Context,
    val preferenceHelper: PreferenceHelper
) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val audioManager: AudioManager =
        context.getSystemService(AUDIO_SERVICE) as AudioManager

    fun play(sessionType: SessionType, insistent: Boolean) {
        try {
            vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (preferenceHelper.isRingtoneEnabled()) {
                val ringtoneRaw =
                    if (sessionType == SessionType.WORK) preferenceHelper.getNotificationSoundWorkFinished()
                    else preferenceHelper.getNotificationSoundBreakFinished()

                val uri = Uri.parse(toRingtone(ringtoneRaw!!).uri)

                mediaPlayer = MediaPlayer()
                mediaPlayer!!.setDataSource(context, uri)
                audioManager.mode = AudioManager.MODE_NORMAL
                val attributes = Builder()
                    .setUsage(if (preferenceHelper.isPriorityAlarm()) USAGE_ALARM else USAGE_NOTIFICATION)
                    .build()
                mediaPlayer!!.setAudioAttributes(attributes)
                mediaPlayer!!.isLooping = insistent
                mediaPlayer!!.prepareAsync()
                mediaPlayer!!.setOnPreparedListener {
                    // TODO: check duration of custom ringtones which may be much longer than notification sounds.
                    // If it's n seconds long and we're in continuous mode,
                    // schedule a stop after x seconds.
                    mediaPlayer!!.start()
                }
            }
            val vibrationType = preferenceHelper.getVibrationType()
            if (vibrationType > 0) {
                vibrator!!.vibrate(
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
        if (mediaPlayer != null && vibrator != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        if (vibrator != null) {
            vibrator!!.cancel()
        }
    }

}