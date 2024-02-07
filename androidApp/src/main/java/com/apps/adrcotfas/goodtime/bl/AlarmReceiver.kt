package com.apps.adrcotfas.goodtime.bl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.java.KoinJavaComponent.inject

class AlarmReceiver: BroadcastReceiver() {

    private val timerManager: TimerManager by inject(TimerManager::class.java)
    override fun onReceive(context: Context, intent: Intent) {
        timerManager.finish()
    }
}