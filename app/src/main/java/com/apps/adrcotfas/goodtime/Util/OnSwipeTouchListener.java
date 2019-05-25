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

package com.apps.adrcotfas.goodtime.Util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private View view;
    private boolean mIsScrolling;

    enum SwipeDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        CENTER
    }

    private SwipeDirection mDirection;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.view = view;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onPress(view);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if(mIsScrolling) {
                mIsScrolling  = false;
                switch (mDirection) {
                    case UP:
                        onSwipeTop(view);
                        break;
                    case DOWN:
                        onSwipeBottom(view);
                        break;
                    case LEFT:
                        onSwipeLeft(view);
                        break;
                    case RIGHT:
                        onSwipeRight(view);
                        break;
                    default:
                        break;
                }
            }
            onRelease(view);
        }
        return gestureDetector.onTouchEvent(event);
    }

    protected abstract void onSwipeRight(View view);
    protected abstract void onSwipeLeft(View view);
    protected abstract void onSwipeBottom(View view);
    protected abstract void onSwipeTop(View view);
    protected abstract void onClick(View view);
    protected abstract void onLongClick(View view);
    protected abstract void onPress(View view);
    protected abstract void onRelease(View view);

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 150;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick(view);
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick(view);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                        mIsScrolling = true;
                        if (diffX > 0) {
                            mDirection = SwipeDirection.RIGHT;
                        } else {
                            mDirection = SwipeDirection.LEFT;
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                    mIsScrolling = true;
                    if (diffY > 0) {
                        mDirection = SwipeDirection.DOWN;
                    } else {
                        mDirection = SwipeDirection.UP;
                    }
                    result = true;
                } else {
                    mDirection = SwipeDirection.CENTER;
                    mIsScrolling = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}