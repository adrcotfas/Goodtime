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
package com.apps.adrcotfas.goodtime.statistics.main

import com.apps.adrcotfas.goodtime.util.StringUtils
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Custom formatter used to print the day of the week or the hour of the day.
 */
internal class ProductiveTimeXAxisFormatter(private val mType: ProductiveTimeType, private val is24HourFormat: Boolean) :
    ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        return if (mType == ProductiveTimeType.HOUR_OF_DAY && value < 24 && value >= 0) {
            StringUtils.toHourOfDay(value.toInt(), is24HourFormat)
        } else if (mType == ProductiveTimeType.DAY_OF_WEEK && value < 7 && value >= 0) {
            StringUtils.toDayOfWeek((value + 1).toInt())
        } else {
            ""
        }
    }
}