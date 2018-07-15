package com.apps.adrcotfas.goodtime.BL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apps.adrcotfas.goodtime.Util.Constants;
import de.greenrobot.event.EventBus;

import static com.apps.adrcotfas.goodtime.BL.CurrentSessionManager.SESSION_TYPE;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(intent.getStringExtra(SESSION_TYPE).equals(SessionType.WORK.toString())?
                new Constants.FinishWorkEvent() : new Constants.FinishBreakEvent());
        context.unregisterReceiver(this);
    }
}
