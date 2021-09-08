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

import org.joda.time.format.DateTimeFormat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

/**
 * Custom X-axis formatter which marks the months and years for an easier reading of the chart:
 * - formats the X-axis labels according to the current range type view (days, weeks or months)
 * - formats the first X-axis label to the current month and year,
 * - formats the first day of a month to the corresponding month
 */
class CustomXAxisFormatter internal constructor(
    private val mDates: List<LocalDate>,
    private var mRangeType: SpinnerRangeType
) : ValueFormatter() {
    private val monthFormatter = DateTimeFormat.forPattern("MMM")
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val firstValue = axis.mEntries[0].toInt()
        val isLeftmost = value == axis.mEntries[0]

        // in case of invalid values
        if (value < 0 || value >= mDates.size || firstValue < 0 || firstValue >= mDates.size) {
            return ""
        }
        val stickyDate = mDates[firstValue]
        val stickyMonth = stickyDate.monthOfYear
        val stickyYear = stickyDate.year
        val stickyText = """
            ${stickyDate.toString(monthFormatter)}
            $stickyYear
            """.trimIndent()
        val crtDate = mDates[value.toInt()]
        val crtDay = crtDate.dayOfMonth
        val crtMonth = crtDate.monthOfYear
        var result: String
        if (isLeftmost) {
            result = stickyText
        } else {
            when (mRangeType) {
                SpinnerRangeType.DAYS -> if (crtDay == 1 && crtMonth != stickyMonth) {
                    result = crtDate.toString(monthFormatter)
                    if (crtDate.monthOfYear == 1 && crtDate.monthOfYear != stickyDate.monthOfYear) {
                        result += """
                            
                            ${crtDate.year}
                            """.trimIndent()
                    }
                } else {
                    result = crtDay.toString()
                }
                SpinnerRangeType.WEEKS -> {
                    var firstMondayOfThisMonth = crtDate.dayOfMonth().withMinimumValue()
                    while (firstMondayOfThisMonth.dayOfWeek != DateTimeConstants.MONDAY) {
                        firstMondayOfThisMonth = firstMondayOfThisMonth.plusDays(1)
                    }
                    val firstMondayOfThisMonthIdx = firstMondayOfThisMonth.dayOfMonth
                    if (crtDay == firstMondayOfThisMonthIdx) {
                        result = crtDate.toString(monthFormatter)
                        if (crtDate.monthOfYear == 1 && crtDate.monthOfYear != stickyDate.monthOfYear) {
                            result += """
                                
                                ${crtDate.year}
                                """.trimIndent()
                        }
                    } else {
                        result = crtDate.weekOfWeekyear.toString()
                    }
                }
                SpinnerRangeType.MONTHS -> {
                    result = crtDate.toString(monthFormatter)
                    if (crtDate.monthOfYear == 1 && crtDate.monthOfYear != stickyDate.monthOfYear) {
                        result += """
                            
                            ${crtDate.year}
                            """.trimIndent()
                    }
                }
            }
        }
        return result
    }

    fun setRangeType(rangeType: SpinnerRangeType) {
        mRangeType = rangeType
    }
}