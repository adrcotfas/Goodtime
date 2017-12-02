package com.apps.adrcotfas.goodtimeplus.View;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;
import com.apps.adrcotfas.goodtimeplus.R;
import com.apps.adrcotfas.goodtimeplus.ViewModel.SessionViewModel;
import com.apps.adrcotfas.goodtimeplus.Model.TimerService;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SessionViewModel mViewModel;

    @BindView(R.id.textView1)  TextView textView;

    @OnClick(R.id.startButton)
    public void onButton1Click() {
        Intent startIntent = new Intent(MainActivity.this, TimerService.class);
        startIntent.setAction(Constants.ACTION.START_TIMER);
        startService(startIntent);
    }

    @OnClick(R.id.stopButton)
    public void onButton2Click() {
        Intent stopIntent = new Intent(MainActivity.this, TimerService.class);
        stopIntent.setAction(Constants.ACTION.STOP_TIMER);
        startService(stopIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

        mViewModel.getSession().getDuration().observe(MainActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long millis) {
                    textView.setText(Long.toString(TimeUnit.MILLISECONDS.toSeconds(millis)));
                    Log.v(TAG, "drawing the time label.");
            }
        });
    }
}
