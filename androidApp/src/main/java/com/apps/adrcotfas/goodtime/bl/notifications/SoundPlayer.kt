package com.apps.adrcotfas.goodtime.bl.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.bl.TimerType
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
    val loop: Boolean,
    val overrideSoundProfile: Boolean
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
    private var overrideSoundProfile = false

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
                    workSoundUri = settings.workFinishedSound,
                    breakSoundUri = settings.breakFinishedSound,
                    overrideSoundProfile = settings.overrideSoundProfile,
                    loop = settings.insistentNotification
                )
            }
                .collect {
                    workRingTone = toSoundData(it.workSoundUri)
                    breakRingTone = toSoundData(it.breakSoundUri)
                    overrideSoundProfile = it.overrideSoundProfile
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
        loop: Boolean = false,
        forceSound: Boolean = false
    ) {
        playerScope.launch {
            job?.cancelAndJoin()
            job = playerScope.launch {
                stopInternal()
                playInternal(soundData, loop, forceSound)
            }
        }
    }

    private fun playInternal(
        soundData: SoundData,
        loop: Boolean,
        forceSound: Boolean
    ) {
        if (soundData.isSilent) return
        val uri = soundData.uriString.let {
            if (it.isEmpty()) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else Uri.parse(it)
        }

        audioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)

        val usage =
            if (areHeadphonesPluggedIn(audioManager!!)) AudioAttributes.USAGE_MEDIA
            else if (overrideSoundProfile || forceSound) AudioAttributes.USAGE_ALARM
            else AudioAttributes.USAGE_NOTIFICATION

        ringtone = RingtoneManager.getRingtone(context, uri).apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(usage)
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

    private fun areHeadphonesPluggedIn(audioManager: AudioManager): Boolean {
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val list = mutableListOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(AudioDeviceInfo.TYPE_BLE_SPEAKER)
        }
        return audioDevices.any { deviceInfo ->
            list.contains(deviceInfo.type)
        }
    }
}