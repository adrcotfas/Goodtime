/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.util

import android.content.Context
import android.view.View.OnTouchListener
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import java.lang.Exception
import kotlin.math.abs

abstract class OnSwipeTouchListener(context: Context?) : OnTouchListener {

    private val gestureDetector: GestureDetector
    private lateinit var view: View

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        this.view = view
        if (event.action == MotionEvent.ACTION_DOWN) {
            onPress(view)
        } else if (event.action == MotionEvent.ACTION_UP) {
            onRelease(view)
        }
        return gestureDetector.onTouchEvent(event)
    }

    protected abstract fun onSwipeRight(view: View)
    protected abstract fun onSwipeLeft(view: View)
    protected abstract fun onSwipeBottom(view: View)
    protected abstract fun onSwipeTop(view: View)
    protected abstract fun onClick(view: View)
    protected abstract fun onLongClick(view: View)
    protected abstract fun onPress(view: View)
    protected abstract fun onRelease(view: View)
    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick(view)
            super.onLongPress(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick(view)
            return super.onSingleTapUp(e)
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(view)
                        } else {
                            onSwipeLeft(view)
                        }
                        result = true
                    }
                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom(view)
                    } else {
                        onSwipeTop(view)
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}