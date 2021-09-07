/*
 * Copyright 2020 Adrian Cotfas
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
import android.os.PowerManager

class BatteryUtils {
    companion object {
        @JvmStatic
        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            val pwrm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pwrm.isIgnoringBatteryOptimizations(context.packageName)
        }
    }
}