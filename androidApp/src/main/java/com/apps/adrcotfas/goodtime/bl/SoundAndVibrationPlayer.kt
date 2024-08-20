package com.apps.adrcotfas.goodtime.bl

class SoundAndVibrationPlayer(
    private val soundPlayer: SoundPlayer,
    private val vibrationPlayer: VibrationPlayer,
) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> {
                if (!event.autoStarted) {
                    soundPlayer.stop()
                    vibrationPlayer.stop()
                }
            }

            is Event.Finished -> {
                soundPlayer.play(event.type)
                vibrationPlayer.start()
            }

            Event.Reset -> {
                soundPlayer.stop()
                vibrationPlayer.stop()
            }

            is Event.AddOneMinute -> {}
            Event.Pause -> {}
        }
    }
}