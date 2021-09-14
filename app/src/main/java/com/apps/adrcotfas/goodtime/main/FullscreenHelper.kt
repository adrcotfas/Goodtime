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
package com.apps.adrcotfas.goodtime.main

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.ActionBar

@SuppressLint("ClickableViewAccessibility")
internal class FullscreenHelper(
    private val mContentView: View,
    private val mActionBar: ActionBar?
) {
    private var mVisible = true
    private val mHideHandler = Handler(Looper.getMainLooper())
    private val mHidePart2Runnable = Runnable {
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private val mShowPart2Runnable = Runnable { // Delayed display of UI elements
        mActionBar!!.show()
    }
    private val mHideRunnable = Runnable { hide() }
    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    fun hide() {
        // Hide UI first
        mActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, AUTO_HIDE_DELAY_MILLIS.toLong())
    }

    fun disable() {
        mContentView.setOnClickListener(null)
        mContentView.setOnTouchListener(null)
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        show()
    }

    companion object {
        private const val AUTO_HIDE = true
        private const val AUTO_HIDE_DELAY_MILLIS = 3000
        private const val UI_ANIMATION_DELAY = 300
    }

    init {
        mContentView.setOnClickListener { toggle() }
        mContentView.setOnTouchListener { _: View?, _: MotionEvent? ->
            if (AUTO_HIDE) {
                delayedHide()
            }
            false
        }
        hide()
    }
}