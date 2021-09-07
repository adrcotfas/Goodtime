package com.apps.adrcotfas.goodtime.statistics.Main;

import android.view.MotionEvent;

import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

public class PieChartGestureListener implements OnChartGestureListener {

    @Override
    public void onChartSingleTapped(MotionEvent me) {}
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
    @Override
    public void onChartLongPressed(MotionEvent me) {}
    @Override
    public void onChartDoubleTapped(MotionEvent me) {}
    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {}
}
