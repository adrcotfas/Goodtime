package com.apps.adrcotfas.goodtimeplus.View;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apps.adrcotfas.goodtimeplus.BL.CurrentSession;
import com.apps.adrcotfas.goodtimeplus.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtimeplus.Util.Constants;
import com.apps.adrcotfas.goodtimeplus.R;
import com.apps.adrcotfas.goodtimeplus.BL.TimerService;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();

    @BindView(R.id.textView1)  TextView textView;

    @OnClick(R.id.startButton)
    public void onStartButtonClick() {
        Intent startIntent = new Intent(MainActivity.this, TimerService.class);
        startIntent.setAction(Constants.ACTION.START_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
    }

    @OnClick(R.id.stopButton)
    public void onStopButtonClick() {
        Intent stopIntent = new Intent(MainActivity.this, TimerService.class);
        stopIntent.setAction(Constants.ACTION.STOP_TIMER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCurrentSession.getDuration().observe(MainActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long millis) {
                    textView.setText(Long.toString(TimeUnit.MILLISECONDS.toSeconds(millis)));
                    Log.v(TAG, "drawing the time label.");
            }
        });
    }
}
