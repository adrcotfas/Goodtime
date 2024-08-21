package com.apps.adrcotfas.goodtime.bl.notifications

import com.apps.adrcotfas.goodtime.bl.Event
import com.apps.adrcotfas.goodtime.bl.EventListener

class SoundVibrationAndTorchPlayer(
    private val soundPlayer: SoundPlayer,
    private val vibrationPlayer: VibrationPlayer,
    private val torchStarter: TorchStarter
) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> {
                if (!event.autoStarted) {
                    soundPlayer.stop()
                    vibrationPlayer.stop()
                    torchStarter.stop()
                }
            }

            is Event.Finished -> {
                soundPlayer.play(event.type)
                vibrationPlayer.start()
                torchStarter.start()
            }

            Event.Reset -> {
                soundPlayer.stop()
                vibrationPlayer.stop()
                torchStarter.stop()
            }

            is Event.AddOneMinute -> {}
            Event.Pause -> {}
        }
    }
}