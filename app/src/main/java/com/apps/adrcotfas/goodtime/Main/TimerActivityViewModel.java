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

package com.apps.adrcotfas.goodtime.Main;

import android.app.Application;

import com.apps.adrcotfas.goodtime.BL.SessionType;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class TimerActivityViewModel extends AndroidViewModel {

    public boolean isActive;
    public boolean showFinishDialog;
    public SessionType dialogPendingType;

    public TimerActivityViewModel(@NonNull Application application) {
        super(application);
        isActive = false;
        showFinishDialog = false;
        dialogPendingType = SessionType.INVALID;
    }
}
