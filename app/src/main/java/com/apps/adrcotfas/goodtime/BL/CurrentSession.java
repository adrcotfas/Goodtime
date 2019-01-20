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

package com.apps.adrcotfas.goodtime.BL;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Stores the session remaining duration, type and timer state.
 * @see GoodtimeApplication
 */
public class CurrentSession {
    private final MutableLiveData<Long> mDuration = new MutableLiveData<>();
    private final MutableLiveData<TimerState> mTimerState = new MutableLiveData<>();
    private final MutableLiveData<SessionType> mSessionType = new MutableLiveData<>();
    private final MutableLiveData<String> mLabel = new MutableLiveData<>();

    public CurrentSession(long duration) {
        this.mDuration.setValue(duration);
        this.mTimerState.setValue(TimerState.INACTIVE);
        this.mSessionType.setValue(SessionType.WORK);
        this.mLabel.setValue(PreferenceHelper.getCurrentSessionLabel().label);
    }

    public LiveData<Long> getDuration() {
        return mDuration;
    }

    public LiveData<TimerState> getTimerState() {
        return mTimerState;
    }

    public LiveData<SessionType> getSessionType() {
        return mSessionType;
    }

    public LiveData<String> getLabel() {
        return mLabel;
    }

    public void setDuration(long newDuration) {
        mDuration.setValue(newDuration);
    }

    public void setTimerState(TimerState state) {
        mTimerState.setValue(state);
    }

    public void setSessionType(SessionType type) {
        mSessionType.setValue(type);
    }

    public void setLabel(String label) {
        mLabel.setValue(label);
    }
}
