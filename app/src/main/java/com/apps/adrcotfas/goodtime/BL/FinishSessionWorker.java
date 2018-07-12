package com.apps.adrcotfas.goodtime.BL;
import android.support.annotation.NonNull;
import com.apps.adrcotfas.goodtime.Util.Constants;
import androidx.work.Worker;
import de.greenrobot.event.EventBus;

public class FinishSessionWorker extends Worker{

    @NonNull
    @Override
    public Result doWork() {
        EventBus.getDefault().post(getTags().contains(SessionType.WORK.toString()) ?
                new Constants.FinishWorkEvent() : new Constants.FinishBreakEvent());
        return Result.SUCCESS;
    }
}
