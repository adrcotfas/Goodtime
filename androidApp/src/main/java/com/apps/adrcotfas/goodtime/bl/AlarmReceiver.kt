package com.apps.adrcotfas.goodtime.bl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver: BroadcastReceiver(), KoinComponent {

    private val timerManager: TimerManager by inject()
    override fun onReceive(context: Context, intent: Intent) {
        timerManager.finish()
    }
}