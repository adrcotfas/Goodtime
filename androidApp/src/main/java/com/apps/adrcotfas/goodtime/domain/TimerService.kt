package com.apps.adrcotfas.goodtime.domain

import android.app.Service
import android.content.Context
import android.content.Intent
import org.koin.java.KoinJavaComponent.inject

class TimerService : Service() {

    private val notificationManager: NotificationArchManager by inject(NotificationArchManager::class.java)
    private val timerManager: TimerManager by inject(TimerManager::class.java)

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val data = timerManager.timerData.value
        when (intent.action) {
            ACTION_START_OR_UPDATE -> {
                startForeground(
                    NotificationArchManager.NOTIFICATION_ID,
                    notificationManager.buildInProgressNotification(data)
                )
            }
            ACTION_RESET -> {
                //TODO: test on minimum SDK version too
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_FINISHED -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                notificationManager.notifyFinished(data)
                return START_NOT_STICKY
            }
            BUTTON_ACTION_PAUSE -> timerManager.pause()
            BUTTON_ACTION_RESUME -> timerManager.start()
            BUTTON_ACTION_ADD_ONE_MIN -> timerManager.addOneMinute()
            BUTTON_ACTION_NEXT -> timerManager.next()
            BUTTON_ACTION_RESET -> timerManager.reset()
        }

        return START_STICKY
    }

    companion object {
        private const val PREFIX = "goodtime.productivity"

        const val ACTION_START_OR_UPDATE = "$PREFIX.ACTION_START_OR_UPDATE"
        const val ACTION_FINISHED = "$PREFIX.ACTION_FINISHED"
        const val ACTION_RESET = "$PREFIX.ACTION_RESET"

        const val BUTTON_ACTION_PAUSE = "$PREFIX.BUTTON_ACTION_PAUSE"
        const val BUTTON_ACTION_RESUME = "$PREFIX.BUTTON_ACTION_RESUME"
        const val BUTTON_ACTION_ADD_ONE_MIN = "$PREFIX.BUTTON_ACTION_ADD_ONE_MIN"
        const val BUTTON_ACTION_NEXT = "$PREFIX.BUTTON_ACTION_NEXT"
        const val BUTTON_ACTION_RESET = "$PREFIX.BUTTON_ACTION_RESET"

        fun createIntentWithAction(context: Context, action: String): Intent {
            return Intent(context, TimerService::class.java).setAction(action)
        }
    }
}