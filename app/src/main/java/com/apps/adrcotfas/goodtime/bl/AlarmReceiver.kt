/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.bl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.greenrobot.eventbus.EventBus
import com.apps.adrcotfas.goodtime.util.Constants.OneMinuteLeft
import com.apps.adrcotfas.goodtime.util.Constants
import com.apps.adrcotfas.goodtime.util.Constants.FinishWorkEvent
import com.apps.adrcotfas.goodtime.util.Constants.FinishBreakEvent
import com.apps.adrcotfas.goodtime.util.Constants.FinishLongBreakEvent

class AlarmReceiver(val listener: OnAlarmReceivedListener) : BroadcastReceiver() {

    interface OnAlarmReceivedListener {
        fun onAlarmReceived()
    }

    companion object{
        private val TAG = AlarmReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        val oneMinuteLeft = intent.getBooleanExtra(Constants.ONE_MINUTE_LEFT, false)
        if (oneMinuteLeft) {
            Log.v(TAG, "onReceive oneMinuteLeft")
            EventBus.getDefault().post(OneMinuteLeft())
            return
        }
        val sessionType = SessionType.valueOf(intent.getStringExtra(Constants.SESSION_TYPE)!!)
        Log.v(TAG, "onReceive $sessionType")
        
        listener.onAlarmReceived()

        when (sessionType) {
            SessionType.WORK -> EventBus.getDefault().post(FinishWorkEvent())
            SessionType.BREAK -> EventBus.getDefault().post(FinishBreakEvent())
            SessionType.LONG_BREAK -> EventBus.getDefault().post(FinishLongBreakEvent())
            else -> {
            }
        }
    }
}