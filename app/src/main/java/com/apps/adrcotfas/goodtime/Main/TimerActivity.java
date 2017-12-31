package com.apps.adrcotfas.goodtime.Main;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.BL.CurrentSession;
import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.BL.TimerService;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerActivity extends AppCompatActivity implements TimerContract.View {

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();

    private TimerContract.Presenter mPresenter;

    @BindView(R.id.timeLabel)  TextView mTimeLabel;

    @OnClick(R.id.timeLabel)
    public void onStartButtonClick() {
        mPresenter.start();
    }

    @OnClick(R.id.stopButton)
    public void onStopButtonClick() {
        mPresenter.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter = new TimerPresenter(this);

        mCurrentSession.getDuration().observe(TimerActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long millis) {
                updateTime(millis);
            }
        });

        //TODO: observe TimerState to animate timer and show extra buttons
        //TODO: observe SessionType to show an icon

//        GoodtimeApplication.getInstance().getBus().getEvents().subscribe(new Consumer<Object>() {
//
//            @Override
//            public void accept(Object o) throws Exception {
//                if (o instanceof Constants.EventType) {
//                    Log.i(TAG,"LALALALALALALA event received");
//                }
//            }
//        });
    }

    @Override
    public void updateTime(Long millis) {
        mTimeLabel.setText(Long.toString(TimeUnit.MILLISECONDS.toSeconds(millis)));
        Log.v(TAG, "drawing the time label.");
    }

    @Override
    public void start() {

        Intent startIntent = new Intent(TimerActivity.this, TimerService.class);
        startIntent.setAction(Constants.ACTION.TOGGLE_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
    }

    @Override
    public void stop() {
        Intent stopIntent = new Intent(TimerActivity.this, TimerService.class);
        stopIntent.setAction(Constants.ACTION.STOP_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }

    @Override
    public void toggle() {

    }

    @Override
    public void skip() {

    }
}
