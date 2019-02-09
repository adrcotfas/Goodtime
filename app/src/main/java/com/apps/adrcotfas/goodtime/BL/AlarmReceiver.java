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

package com.apps.adrcotfas.goodtime.BL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;
import de.greenrobot.event.EventBus;

import static com.apps.adrcotfas.goodtime.Util.Constants.SESSION_TYPE;

public class AlarmReceiver extends BroadcastReceiver {

    private final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        final SessionType sessionType = SessionType.valueOf(intent.getStringExtra(SESSION_TYPE));
        Log.v(TAG, "onReceive " + sessionType.toString());

        GoodtimeApplication.getInstance().getCurrentSession().setTimerState(TimerState.INACTIVE);

        switch (sessionType) {
            case WORK:
                EventBus.getDefault().post(new Constants.FinishWorkEvent());
                break;
            case BREAK:
                EventBus.getDefault().post(new Constants.FinishBreakEvent());
                break;
            case LONG_BREAK:
                EventBus.getDefault().post(new Constants.FinishLongBreakEvent());
                break;
            default:
                break;
        }
    }
}
