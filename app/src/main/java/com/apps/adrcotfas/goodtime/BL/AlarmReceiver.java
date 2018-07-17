package com.apps.adrcotfas.goodtime.BL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;
import de.greenrobot.event.EventBus;

import static com.apps.adrcotfas.goodtime.BL.CurrentSessionManager.SESSION_TYPE;

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive " + intent.getStringExtra(SESSION_TYPE));

        ((GoodtimeApplication) context.getApplicationContext())
                .getCurrentSession().setTimerState(TimerState.INACTIVE);

        EventBus.getDefault().post(intent.getStringExtra(SESSION_TYPE).equals(SessionType.WORK.toString())?
                new Constants.FinishWorkEvent() : new Constants.FinishBreakEvent());
    }
}
