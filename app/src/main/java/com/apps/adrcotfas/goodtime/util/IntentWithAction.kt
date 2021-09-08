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
import android.content.Intent
import com.apps.adrcotfas.goodtime.bl.SessionType
import com.apps.adrcotfas.goodtime.util.Constants.SESSION_TYPE

class IntentWithAction : Intent {
    constructor(context: Context, cls: Class<*>, action: String) : super(context, cls) {
        this.action = action
    }

    constructor(
        context: Context,
        cls: Class<*>,
        action: String,
        sessionType: SessionType
    ) : super(context, cls) {
        this.action = action
        this.putExtra(SESSION_TYPE, sessionType.toString())
    }
}