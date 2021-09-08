package com.apps.adrcotfas.goodtime.statistics.main

import android.view.MotionEvent
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.OnChartGestureListener

open class PieChartGestureListener : OnChartGestureListener {
    override fun onChartSingleTapped(me: MotionEvent) {}
    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture) {}
    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture) {}
    override fun onChartLongPressed(me: MotionEvent) {}
    override fun onChartDoubleTapped(me: MotionEvent) {}
    override fun onChartFling(
        me1: MotionEvent,
        me2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ) {
    }

    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {}
    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {}
}