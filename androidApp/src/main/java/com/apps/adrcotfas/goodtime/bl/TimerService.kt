package com.apps.adrcotfas.goodtime.bl

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
            Action.StartOrUpdate.name -> {
                startForeground(
                    NotificationArchManager.NOTIFICATION_ID,
                    notificationManager.buildInProgressNotification(data)
                )
            }
            Action.Reset.name -> {
                //TODO: test on minimum SDK version too
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                timerManager.reset()
                return START_NOT_STICKY
            }
            Action.Finished.name -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                val autoStart = intent.getBooleanExtra(EXTRA_FINISHED_AUTOSTART, false)
                if (!autoStart) {
                    notificationManager.notifyFinished(data)
                }
                return START_NOT_STICKY
            }
            Action.Pause.name -> timerManager.pause()
            Action.Resume.name -> timerManager.start()
            Action.AddOneMinute.name -> timerManager.addOneMinute()
            Action.Next.name -> timerManager.next()
        }

        return START_STICKY
    }

    companion object {
        enum class Action {
            StartOrUpdate,
            Reset,
            Finished,
            Pause,
            Resume,
            AddOneMinute,
            Next
        }

        private const val EXTRA_FINISHED_AUTOSTART = "EXTRA_FINISHED_AUTOSTART"

        fun createIntentWithAction(context: Context, action: Action): Intent {
            return Intent(context, TimerService::class.java).setAction(action.name)
        }

        fun createFinishEvent(context: Context, autostart: Boolean = false): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = Action.Finished.name
                putExtra(EXTRA_FINISHED_AUTOSTART, autostart)
            }
        }
    }
}