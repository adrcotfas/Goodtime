package com.apps.adrcotfas.goodtime.bl

import android.content.Context

class TimerServiceHandler(private val context: Context) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> startService()
            is Event.Pause -> startService()
            is Event.AddOneMinute -> startService()

            is Event.Reset -> context.startService(
                TimerService.createIntentWithAction(
                    context,
                    TimerService.ACTION_RESET
                )
            )

            is Event.Finished -> {
                context.startService(
                    TimerService.createIntentWithAction(
                        context,
                        TimerService.ACTION_FINISHED
                    )
                )
            }
        }
    }

    private fun startService() {
        context.startService(
            TimerService.createIntentWithAction(
                context,
                TimerService.ACTION_START_OR_UPDATE
            )
        )
    }
}