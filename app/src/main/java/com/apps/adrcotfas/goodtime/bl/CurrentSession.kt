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
package com.apps.adrcotfas.goodtime.bl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

/**
 * Stores the session remaining duration, type and timer state.
 * @see GoodtimeApplication
 */
class CurrentSession(durationInput: Long, label: String) {

    private val _duration = MutableLiveData<Long>()
    private val _timerState = MutableLiveData<TimerState>()
    private val _sessionType = MutableLiveData<SessionType>()
    private val _label = MutableLiveData<String>()

    init {
        _duration.value = durationInput
        _timerState.value = TimerState.INACTIVE
        _sessionType.value = SessionType.WORK
        _label.value = label
    }

    val duration: LiveData<Long>
        get() = _duration
    val timerState: LiveData<TimerState>
        get() = _timerState
    val sessionType: LiveData<SessionType>
        get() = _sessionType
    val label: LiveData<String>
        get() = _label

    fun setDuration(newDuration: Long) {
        _duration.value = newDuration
    }

    fun setTimerState(state: TimerState) {
        _timerState.value = state
    }

    fun setSessionType(type: SessionType) {
        _sessionType.value = type
    }

    fun setLabel(label: String) {
        _label.value = label
    }
}