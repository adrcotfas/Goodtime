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

        final SessionType sessionType = SessionType.valueOf(intent.getStringExtra(SESSION_TYPE));
        Log.v(TAG, "onReceive " + sessionType.toString());

        GoodtimeApplication.getInstance().getCurrentSession().setTimerState(TimerState.INACTIVE);

        if (PreferenceHelper.isLongBreakEnabled()) {
            if (sessionType == SessionType.LONG_BREAK) {
                PreferenceHelper.resetCurrentStreak();
            } else if (sessionType == SessionType.WORK){
                PreferenceHelper.incrementCurrentStreak();
            }

            Log.v(TAG, "PreferenceHelper.getCurrentStreak: " + PreferenceHelper.getCurrentStreak());
            Log.v(TAG, "PreferenceHelper.lastWorkFinishedAt: " + PreferenceHelper.lastWorkFinishedAt());
        }

        EventBus.getDefault().post(sessionType == SessionType.WORK ?
                new Constants.FinishWorkEvent() : new Constants.FinishBreakEvent());
    }
}
