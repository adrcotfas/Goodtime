package com.apps.adrcotfas.goodtime.bl

import android.content.Context
import com.apps.adrcotfas.goodtime.bl.TimerService.Companion.Action

class TimerServiceStarter(private val context: Context) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> startService()
            is Event.Pause -> startService()
            is Event.AddOneMinute -> startService()
            is Event.Reset -> startService(Action.Reset)
            is Event.Finished -> startServiceWithFinished(event.autostartNextSession)
        }
    }

    private fun startService(action: Action = Action.StartOrUpdate) {
        context.startService(
            TimerService.createIntentWithAction(
                context,
                action
            )
        )
    }

    private fun startServiceWithFinished(autoStart: Boolean) {
        context.startService(
            TimerService.createFinishEvent(
                context,
                autoStart
            )
        )
    }
}