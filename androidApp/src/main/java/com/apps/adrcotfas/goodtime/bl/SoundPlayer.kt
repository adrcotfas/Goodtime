package com.apps.adrcotfas.goodtime.bl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.SoundData
import com.apps.adrcotfas.goodtime.settings.notification_sounds.toSoundData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.reflect.Method

data class SoundPlayerData(
    val workSoundUri: String,
    val breakSoundUri: String,
    val loop: Boolean
)

class SoundPlayer(
    private val context: Context,
    readFromSettingsScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val playerScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val settingsRepo: SettingsRepository,
    private val logger: Logger
) {
    private var job: Job? = null

    private var audioManager: AudioManager? = null
    private var ringtone: Ringtone? = null
    private lateinit var setLoopingMethod: Method

    private var workRingTone = SoundData()
    private var breakRingTone = SoundData()
    private var loop = false

    init {
        try {
            setLoopingMethod =
                Ringtone::class.java.getDeclaredMethod(
                    "setLooping",
                    Boolean::class.javaPrimitiveType
                )
        } catch (e: NoSuchMethodException) {
            logger.e(e) { "Failed to get method setLooping" }
        }
        readFromSettingsScope.launch {
            settingsRepo.settings.map { settings ->
                SoundPlayerData(
                    settings.workFinishedSound,
                    settings.breakFinishedSound,
                    settings.insistentNotification
                )
            }
                .collect {
                    workRingTone = toSoundData(it.workSoundUri)
                    breakRingTone = toSoundData(it.breakSoundUri)
                    loop = it.loop
                }
        }
    }

    fun play(timerType: TimerType) {
        val soundData = when (timerType) {
            TimerType.WORK -> workRingTone
            TimerType.BREAK, TimerType.LONG_BREAK -> breakRingTone
        }
        play(soundData, loop)
    }

    fun play(
        soundData: SoundData,
        loop: Boolean = false
    ) {
        playerScope.launch {
            job?.cancelAndJoin()
            job = playerScope.launch {
                stopInternal()
                playInternal(soundData, loop)
            }
        }
    }

    private fun playInternal(
        soundData: SoundData,
        loop: Boolean
    ) {
        if (soundData.isSilent) return
        val uri = soundData.uriString.let {
            if (it.isEmpty()) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else Uri.parse(it)
        }

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        ringtone = RingtoneManager.getRingtone(context, uri).apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }
        try {
            if (loop) {
                setLoopingMethod.invoke(ringtone, true)
            }
        } catch (e: Throwable) {
            logger.e(e) { "Failed to set looping" }
        }
        try {
            ringtone!!.play()
        } catch (e: Throwable) {
            logger.e(e) { "Failed to play ringtone" }
        }
    }

    fun stop() {
        playerScope.launch {
            job?.cancelAndJoin()
            job = playerScope.launch {
                stopInternal()
            }
        }
    }

    private fun stopInternal() {
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        ringtone = null
    }
}