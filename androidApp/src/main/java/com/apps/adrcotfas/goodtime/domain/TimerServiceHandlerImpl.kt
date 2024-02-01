package com.apps.adrcotfas.goodtime.domain

import android.content.Context

class TimerServiceHandlerImpl(private val context: Context) : TimerServiceHandler {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start, Event.Pause, Event.NextSession, Event.AddOneMinute -> context.startService(
                TimerService.createIntentWithAction(
                    context,
                    TimerService.ACTION_START_OR_UPDATE
                )
            )

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
}