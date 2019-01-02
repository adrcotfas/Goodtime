package com.apps.adrcotfas.goodtime.BL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;
import de.greenrobot.event.EventBus;

import static com.apps.adrcotfas.goodtime.Util.Constants.SESSION_TYPE;

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = AlarmReceiver.class.getSimpleName();

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
