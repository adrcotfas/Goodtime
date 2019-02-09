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

package com.apps.adrcotfas.goodtime.Statistics.AllSessions;

import org.joda.time.DateTime;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddEditEntryDialogViewModel extends ViewModel {

    public static int INVALID_SESSION_TO_EDIT_ID = -1;
    public MutableLiveData<Integer> duration = new MutableLiveData<>();
    public MutableLiveData<DateTime> date = new MutableLiveData<>();
    public MutableLiveData<String> label = new MutableLiveData<>();
    public long sessionToEditId;

    public AddEditEntryDialogViewModel() {
        date.setValue(new DateTime().withHourOfDay(9).withMinuteOfHour(0));
        label.setValue(null);
        sessionToEditId = INVALID_SESSION_TO_EDIT_ID;
    }
}
