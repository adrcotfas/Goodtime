/*
 * Copyright 2016-2021 Adrian Cotfas
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

import com.apps.adrcotfas.goodtime.util.TimeUtils.Companion.firstDayOfWeek
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters

/**
 * Custom X-axis formatter which marks the months and years for an easier reading of the chart:
 * - formats the X-axis labels according to the current range type view (days, weeks or months)
 * - formats the first X-axis label to the current month and year,
 * - formats the first day of a month to the corresponding month
 */
class DayWeekMonthXAxisFormatter internal constructor(
    private val dates: List<LocalDate>,
    private var rangeType: HistorySpinnerRangeType
) : ValueFormatter() {

    private val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val firstValue = axis.mEntries[0].toInt()
        val isLeftmost = value == axis.mEntries[0]

        // in case of invalid values
        if (value < 0 || value >= dates.size || firstValue < 0 || firstValue >= dates.size) {
            return ""
        }
        val stickyDate = dates[firstValue]
        val stickyMonth = stickyDate.month.value
        val stickyYear = stickyDate.year
        val stickyText = "${stickyDate.format(monthFormatter)}\n$stickyYear"
        val crtDate = dates[value.toInt()]
        val crtDay = crtDate.dayOfMonth
        val crtMonth = crtDate.month.value
        var result: String
        if (isLeftmost) {
            result = stickyText
        } else {
            when (rangeType) {
                HistorySpinnerRangeType.DAYS -> if (crtDay == 1 && crtMonth != stickyMonth) {
                    result = crtDate.format(monthFormatter)
                    if (crtMonth == 1) {
                        result += "\n${crtDate.year}"
                    }
                } else {
                    result = crtDay.toString()
                }
                HistorySpinnerRangeType.WEEKS -> {
                    val firstWeekStartThisMonth = crtDate.with(
                        TemporalAdjusters.firstInMonth(
                            firstDayOfWeek()
                        )
                    )
                    val firstWeekStartThisMonthIdx = firstWeekStartThisMonth.dayOfMonth
                    if (crtDay == firstWeekStartThisMonthIdx) {
                        result = crtDate.format(monthFormatter)
                        if (crtMonth == 1 && crtMonth != stickyMonth) {
                            result += "\n${crtDate.year}"
                        }
                    } else {
                        result = crtDate.with(
                            TemporalAdjusters.previousOrSame(
                                firstDayOfWeek()
                            )
                        ).get(ChronoField.ALIGNED_WEEK_OF_YEAR).toString()
                    }
                }
                HistorySpinnerRangeType.MONTHS -> {
                    result = crtDate.format(monthFormatter)
                    val monthValue = crtDate.monthValue
                    if (monthValue == 1 && monthValue != stickyDate.monthValue) {
                        result += "\n${crtDate.year}"
                    }
                }
            }
        }
        return result
    }

    fun setRangeType(rangeType: HistorySpinnerRangeType) {
        this.rangeType = rangeType
    }
}