package com.apps.adrcotfas.goodtime.bl

import android.app.Service
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.di.injectLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

//TODO: fix the following:
// wait for session to finish,
// close the app and then tap continue from the notification => nothing happens
class TimerService : Service(), KoinComponent {

    private val notificationManager: NotificationArchManager by inject()
    private val timerManager: TimerManager by inject()
    private val log: Logger by injectLogger("TimerService")
    private val soundPlayer: SoundPlayer by inject()

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val data = timerManager.timerData.value
        log.i { "onStartCommand: ${intent.action}" }
        when (intent.action) {
            Action.StartOrUpdate.name -> {
                notificationManager.clearFinishedNotification()
                startForeground(
                    NotificationArchManager.IN_PROGRESS_NOTIFICATION_ID,
                    notificationManager.buildInProgressNotification(data)
                )
            }

            Action.Reset.name -> {
                //TODO: test on minimum SDK version too
                notificationManager.clearFinishedNotification()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            Action.Finished.name -> {
                val autoStart = intent.getBooleanExtra(EXTRA_FINISHED_AUTOSTART, false)
                if (!autoStart) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                notificationManager.notifyFinished(data, withActions = !autoStart)
                soundPlayer.play(context = applicationContext, data.type)
                return START_NOT_STICKY
            }

            // actions triggered from the notification itself
            Action.Toggle.name -> timerManager.toggle()
            Action.AddOneMinute.name -> timerManager.addOneMinute()
            Action.Next.name -> timerManager.next()
            Action.DoReset.name -> timerManager.reset()
        }

        return START_STICKY
    }

    companion object {
        enum class Action {
            StartOrUpdate,
            Reset,
            Finished,

            // actions triggered from the notification itself
            Toggle,
            AddOneMinute,
            Next,
            DoReset
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