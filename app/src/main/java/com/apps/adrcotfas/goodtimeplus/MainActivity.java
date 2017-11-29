package com.apps.adrcotfas.goodtimeplus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button1) Button button1;
    @BindView(R.id.button2) Button button2;
    @BindView(R.id.textView1)  TextView textView;

    @OnClick(R.id.button1)
    public void onButton1Click() {
        Intent startIntent = new Intent(MainActivity.this, TimerService.class);
        startIntent.setAction(Constants.ACTION.START_TIMER);
        startService(startIntent);
    }

    @OnClick(R.id.button2)
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
    }
}
