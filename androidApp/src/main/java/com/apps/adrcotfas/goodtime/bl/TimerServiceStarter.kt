package com.apps.adrcotfas.goodtime.bl

import android.content.Context

class TimerServiceStarter(private val context: Context) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> startService()
            is Event.Pause -> startService()
            is Event.AddOneMinute -> startService()
            is Event.Reset -> startService(TimerService.ACTION_RESET)
            is Event.Finished -> startService(TimerService.ACTION_FINISHED)
        }
    }

    private fun startService(action: String = TimerService.ACTION_START_OR_UPDATE) {
        context.startService(
            TimerService.createIntentWithAction(
                context,
                action
            )
        )
    }

}