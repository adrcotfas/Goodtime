package com.apps.adrcotfas.goodtime.bl.notifications

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class VibrationData(
    val strength: Int,
    val loop: Boolean
)

class VibrationPlayer(
    context: Context,
    private val settingsRepo: SettingsRepository,
    readFromSettingsScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {

    private var data: VibrationData = VibrationData(3, false)

    init {
        readFromSettingsScope.launch {
            settingsRepo.settings.map {
                VibrationData(
                    it.vibrationStrength,
                    it.insistentNotification
                )
            }.collect {
                data = it
            }
        }
    }

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    fun start() {
        start(data)
    }

    fun stop() {
        vibrator.cancel()
    }

    fun start(strength: Int) {
        start(VibrationData(strength, false))
    }

    private fun start(data: VibrationData) {
        vibrator.cancel()
        val (strength, loop) = data
        val repeat = if (loop) 2 else -1
        if (strength == 0 || !vibrator.hasVibrator()) {
            return
        }
        val pattern = when (strength) {
            1 -> {
                longArrayOf(0, 100, 2000)
            }
            2 -> {
                longArrayOf(0, 100, 50, 100, 2000)
            }
            3 -> {
                longArrayOf(0, 200, 50, 200, 2000)
            }
            4 -> {
                longArrayOf(0, 400, 100, 400, 2000)
            }
            5 -> {
                longArrayOf(0, 400, 100, 400, 100, 400, 2000)
            }
            else -> longArrayOf()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }
}