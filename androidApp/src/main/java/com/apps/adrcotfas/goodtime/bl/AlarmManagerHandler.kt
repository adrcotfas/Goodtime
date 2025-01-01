/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.bl

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.bl.TimeUtils.formatMilliseconds

class AlarmManagerHandler(
    private val context: Context,
    private val timeProvider: TimeProvider,
    private val log: Logger,
) : EventListener {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun onEvent(event: Event) {
        cancelAlarm()
        when (event) {
            is Event.Start -> {
                if (event.endTime == 0L) {
                    return
                }
                setAlarm(event.endTime)
            }

            is Event.AddOneMinute -> {
                setAlarm(event.endTime)
            }

            // TODO: consider the case with keeping the screen on
            // and triggering a finish event when the timer is done: the alarm should be canceled
            else -> {
                // do nothing
            }
        }
    }

    // USE_EXACT_ALARM permission is used instead which is granted at install and cannot be revoked
    @SuppressLint("MissingPermission")
    private fun setAlarm(triggerAtMillis: Long) {
        log.v { "Set alarm in ${(triggerAtMillis - timeProvider.elapsedRealtime()).formatMilliseconds()} from now" }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            getAlarmPendingIntent(),
        )
    }

    private fun cancelAlarm() {
        log.v { "Cancel the alarm, if any" }
        alarmManager.cancel(getAlarmPendingIntent())
    }

    private fun getAlarmPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
