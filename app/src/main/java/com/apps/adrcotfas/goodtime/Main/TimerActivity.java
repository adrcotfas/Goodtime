package com.apps.adrcotfas.goodtime.Main;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.BL.CurrentSession;
import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.BL.TimerService;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

public class TimerActivity extends AppCompatActivity {

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();


    @BindView(R.id.timeLabel)  TextView mTimeLabel;

    @OnClick(R.id.timeLabel)
    public void onStartButtonClick() {
        start();
    }

    @OnClick(R.id.stopButton)
    public void onStopButtonClick() {
        stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCurrentSession.getDuration().observe(TimerActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long millis) {
                updateTime(millis);
            }
        });

        //TODO: observe TimerState to animate timer and show extra buttons
        //TODO: observe SessionType to show an icon

        GoodtimeApplication.getInstance().getBus().getEvents().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof Constants.FinishEvent) {
                    finish();
                }
            }
        });
    }

    public void updateTime(Long millis) {
        mTimeLabel.setText(Long.toString(TimeUnit.MILLISECONDS.toSeconds(millis)));
        Log.v(TAG, "drawing the time label.");
    }

    public void start() {
        Intent startIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.TOGGLE_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
    }

    public void stop() {
        Intent stopIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.STOP_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }

    public void toggle() {

    }

    public void skip() {

    }

    public void finish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Test")
                .setTitle("test");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
