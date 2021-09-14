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

import com.apps.adrcotfas.goodtime.statistics.ChartMarker
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.google.android.material.card.MaterialCardView
import com.github.mikephil.charting.charts.PieChart
import android.widget.LinearLayout
import android.widget.Spinner
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.statistics.SessionViewModel
import android.widget.ProgressBar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import android.view.MotionEvent
import com.github.mikephil.charting.formatter.PercentFormatter
import android.text.TextPaint
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentMainBinding
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.animation.Easing
import com.apps.adrcotfas.goodtime.util.StringUtils
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.lang.StringBuilder
import java.util.*
import kotlin.math.ceil

@AndroidEntryPoint
class StatisticsFragment : Fragment() {
    //TODO: move to separate file
    private class Stats(
        var today: Long,
        var week: Long,
        var month: Long,
        var total: Long
    )

    //TODO: move to separate file and remove duplicate code
    private class StatsView(
        var today: TextView,
        var week: TextView,
        var month: TextView,
        var total: TextView
    )

    private var sessionsToObserve: LiveData<List<Session>>? = null
    private lateinit var chartHistory: LineChart
    private lateinit var chartProductiveHours: BarChart
    private var showPieChart = false
    private lateinit var pieChartSection: MaterialCardView
    private lateinit var pieChart: PieChart
    private var pieChartShowPercentages = false
    private lateinit var pieEmptyState: LinearLayout
    private lateinit var statsType: Spinner
    private lateinit var rangeType: Spinner
    private lateinit var productiveTimeType: Spinner
    private lateinit var pieChartType: Spinner
    private lateinit var headerOverview: TextView
    private lateinit var headerHistory: TextView
    private lateinit var headerProductiveTime: TextView
    private lateinit var xAxisFormatter: CustomXAxisFormatter
    private val xValues: MutableList<LocalDate> = ArrayList()
    private lateinit var overview: StatsView
    private lateinit var overviewDescription: StatsView

    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val labelsViewModel: LabelsViewModel by activityViewModels()

    private lateinit var parentView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var displayDensity = 1f
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: StatisticsFragmentMainBinding = DataBindingUtil.inflate(
            inflater, R.layout.statistics_fragment_main, container, false
        )
        val view = binding.root
        displayDensity = resources.displayMetrics.density
        setHasOptionsMenu(true)
        parentView = binding.parentLayout
        progressBar = binding.progressBar
        chartHistory = binding.history.chart
        chartProductiveHours = binding.productiveHours.barChart
        overview = StatsView(
            binding.overview.todayValue,
            binding.overview.weekValue,
            binding.overview.monthValue,
            binding.overview.totalValue
        )
        overviewDescription = StatsView(
            binding.overview.todayDescription,
            binding.overview.weekDescription,
            binding.overview.monthDescription,
            binding.overview.totalDescription
        )
        statsType = binding.overview.statsType
        rangeType = binding.history.rangeType
        productiveTimeType = binding.productiveHours.timeType
        pieChartType = binding.pieChartSection.pieChartType
        headerOverview = binding.overview.header
        headerHistory = binding.history.headerHistory
        headerProductiveTime = binding.productiveHours.headerProductiveTime
        pieChartSection = binding.pieChartSection.parent
        pieChart = binding.pieChartSection.pieChart
        pieEmptyState = binding.pieChartSection.emptyState
        setupPieChart()
        labelsViewModel.crtExtendedLabel.observe(
            viewLifecycleOwner,
            { refreshUi() })
        setupSpinners()
        setupHistoryChart()
        setupProductiveTimeChart()

        // TODO: remove this later
//        for (int i = 0; i < 1000; ++i) {
//            Session session = new Session(
//                    0,
//                    System.currentTimeMillis(),
//                    42,
//                    null);
//
//            mSessionViewModel.addSession(session);
//        }
        return view
    }

    private fun refreshStats(sessions: List<Session>) {
        val isDurationType = statsType.selectedItemPosition == SpinnerStatsType.DURATION.ordinal
        val today = LocalDate()
        val thisWeekStart = today.dayOfWeek().withMinimumValue()
        val thisWeekEnd = today.dayOfWeek().withMaximumValue()
        val thisMonthStart = today.dayOfMonth().withMinimumValue()
        val thisMonthEnd = today.dayOfMonth().withMaximumValue()
        val stats = Stats(0, 0, 0, 0)
        for (s in sessions) {
            val increment = if (isDurationType) s.duration.toLong() else 1L
            val crt = LocalDate(Date(s.timestamp))
            if (crt.isEqual(today)) {
                stats.today += increment
            }
            if (crt.isAfter(thisWeekStart.minusDays(1)) && crt.isBefore(thisWeekEnd.plusDays(1))) {
                stats.week += increment
            }
            if (crt.isAfter(thisMonthStart.minusDays(1)) && crt.isBefore(thisMonthEnd.plusDays(1))) {
                stats.month += increment
            }
            if (isDurationType) {
                stats.total += increment
            }
        }
        if (!isDurationType) {
            stats.total += sessions.size.toLong()
        }
        overview.apply {
            this.today.text =
                if (isDurationType) StringUtils.formatMinutes(stats.today) else StringUtils.formatLong(
                    stats.today
                )
            week.text =
                if (isDurationType) StringUtils.formatMinutes(stats.week) else StringUtils.formatLong(
                    stats.week
                )
            month.text =
                if (isDurationType) StringUtils.formatMinutes(stats.month) else StringUtils.formatLong(
                    stats.month
                )
            total.text =
                if (isDurationType) StringUtils.formatMinutes(stats.total) else StringUtils.formatLong(
                    stats.total
                )
        }
        overviewDescription.week.text =
            "${resources.getString(R.string.statistics_week)} ${thisWeekStart.weekOfWeekyear}"
        val sb = StringBuilder(thisMonthEnd.toString("MMMM"))
        if (sb.isNotEmpty()) {
            sb.setCharAt(0, Character.toUpperCase(sb[0]))
        }
        overviewDescription.month.text = sb.toString()
    }

    private fun setupSpinners() {
        val statsTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_stats_type, R.layout.spinner_item
        )
        statsTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        statsType.apply {
            adapter = statsTypeAdapter
            setSelection(1, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    refreshUi()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        }
        val rangeTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_range_type, R.layout.spinner_item
        )
        rangeTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        rangeType.apply {
            adapter = rangeTypeAdapter
            setSelection(0, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    xAxisFormatter.setRangeType(SpinnerRangeType.values()[position])
                    refreshUi()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        }
        val timeTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_productive_time_type, R.layout.spinner_item
        )
        timeTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        productiveTimeType.apply {
            adapter = timeTypeAdapter
            setSelection(0, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                    refreshUi()
                }
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        }
        val pieTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_pie_time_type, R.layout.spinner_item
        )
        pieTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        pieChartType.apply {
            adapter = pieTypeAdapter
            setSelection(2, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    refreshUi()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun refreshProductiveTimeChart(sessions: List<Session>, color: Int) {
        // generate according to spinner
        if (productiveTimeType.selectedItemPosition == SpinnerProductiveTimeType.HOUR_OF_DAY.ordinal) {
            generateProductiveTimeChart(sessions, SpinnerProductiveTimeType.HOUR_OF_DAY, color)
            val visibleXCount = ThemeHelper.pxToDp(requireContext(), chartHistory.width).toInt() / 36
            chartProductiveHours.apply {
                setVisibleXRangeMaximum(visibleXCount.toFloat())
                setVisibleXRangeMinimum(visibleXCount.toFloat())
                xAxis.labelCount = visibleXCount
            }
        } else {
            generateProductiveTimeChart(sessions, SpinnerProductiveTimeType.DAY_OF_WEEK, color)
            chartProductiveHours.apply {
                setVisibleXRangeMaximum(7f)
                setVisibleXRangeMinimum(7f)
                xAxis.labelCount = 7
            }
        }
        chartProductiveHours.barData.setDrawValues(false)
        val b = chartProductiveHours.data.dataSets[0]
        var maxY = 0f
        var maxIdx = 0
        for (i in 0 until b.entryCount) {
            val crtY = b.getEntryForIndex(i).y
            if (crtY > maxY) {
                maxY = crtY
                maxIdx = i
            }
        }
        chartProductiveHours.apply {
            moveViewToX(maxIdx.toFloat())
            invalidate()
            notifyDataSetChanged()
        }
    }

    //TODO: make more efficient when setting spinners to not refresh all of it if not needed
    private fun refreshUi() {
        val label = labelsViewModel.crtExtendedLabel.value
        if (label != null) {
            sessionsToObserve?.removeObservers(this)
            val color = ThemeHelper.getColor(requireContext(), label.colorId)
            overview.apply {
                today.setTextColor(color)
                week.setTextColor(color)
                month.setTextColor(color)
                total.setTextColor(color)
            }
            headerOverview.setTextColor(color)
            headerHistory.setTextColor(color)
            headerProductiveTime.setTextColor(color)
            when (label.title) {
                getString(R.string.label_all) -> {
                    sessionsToObserve = sessionViewModel.allSessionsByEndTime
                    showPieChart = true
                }
                "unlabeled" -> {
                    sessionsToObserve = sessionViewModel.allSessionsUnlabeled
                    showPieChart = false
                }
                else -> {
                    sessionsToObserve = sessionViewModel.getSessions(label.title)
                    showPieChart = false
                }
            }
            sessionsToObserve?.observe(viewLifecycleOwner, { sessions: List<Session> ->
                refreshStats(sessions)
                refreshHistoryChart(sessions, color)
                refreshProductiveTimeChart(sessions, color)
                pieChartSection.visibility = if (showPieChart) View.VISIBLE else View.GONE
                if (showPieChart) {
                    val labelsLd = labelsViewModel.labels
                    labelsLd.observe(viewLifecycleOwner, { labels: List<Label> ->
                        labelsLd.removeObservers(requireActivity())
                        refreshPieChart(sessions, labels)
                    })
                }
                lifecycleScope.launch {
                    delay(100)
                    progressBar.visibility = View.GONE
                    parentView.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun setupPieChart() {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        @ColorInt val color = typedValue.data
        pieChart.apply {
            setHoleColor(color)
            legend.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 80f
            description.isEnabled = false
            setExtraOffsets(8f, 8f, 8f, 8f)
            highlightValues(null)
            setEntryLabelColor(resources.getColor(R.color.grey_500))
            setEntryLabelTextSize(11f)
            transparentCircleRadius = 0f
            dragDecelerationFrictionCoef = 0.95f
            isRotationEnabled = true
            isHighlightPerTapEnabled = false
            setNoDataText("")
            setTouchEnabled(true)
            onChartGestureListener = object : PieChartGestureListener() {
                override fun onChartSingleTapped(me: MotionEvent) {
                    pieChartShowPercentages = !pieChartShowPercentages
                    refreshUi()
                }
            }
        }
    }

    private fun refreshPieChart(sessions: List<Session>, labels: List<Label>) {
        // Nullable String key for the unlabeled sessions
        val totalTimePerLabel: MutableMap<String?, Int> = HashMap()
        val pieStatsType = PieStatsType.values()[pieChartType.selectedItemPosition]
        val today = LocalDate()
        val todayStart = today.toDateTimeAtStartOfDay().millis
        val thisWeekStart = today.dayOfWeek().withMinimumValue().toDateTimeAtStartOfDay().millis
        val thisMonthStart = today.dayOfMonth().withMinimumValue().toDateTimeAtStartOfDay().millis
        val filteredSessions: MutableList<Session> = ArrayList(sessions)
        when (pieStatsType) {
            PieStatsType.TODAY -> for (s in sessions) {
                if (s.timestamp < todayStart) {
                    filteredSessions.remove(s)
                }
            }
            PieStatsType.THIS_WEEK -> for (s in sessions) {
                if (s.timestamp < thisWeekStart) {
                    filteredSessions.remove(s)
                }
            }
            PieStatsType.THIS_MONTH -> for (s in sessions) {
                if (s.timestamp < thisMonthStart) {
                    filteredSessions.remove(s)
                }
            }
            PieStatsType.TOTAL -> {
            }
        }
        if (filteredSessions.isEmpty()) {
            pieEmptyState.visibility = View.VISIBLE
            pieChart.visibility = View.GONE
            return
        } else {
            pieEmptyState.visibility = View.GONE
            pieChart.visibility = View.VISIBLE
        }
        for (s in filteredSessions) {
            if (!s.archived) {
                if (totalTimePerLabel.containsKey(s.label)) {
                    totalTimePerLabel[s.label] = totalTimePerLabel[s.label]!! + s.duration
                } else {
                    totalTimePerLabel[s.label] = s.duration
                }
            }
        }
        val entries = ArrayList<PieEntry>()
        for (label in totalTimePerLabel.keys) {
            entries.add(PieEntry(totalTimePerLabel[label]!!.toFloat(), label))
        }
        entries.sortWith { o1: PieEntry, o2: PieEntry ->
            o2.value.compareTo(o1.value)
        }
        val colors = ArrayList<Int>()
        for (p in entries) {
            if (labels.isEmpty()) {
                p.label = getString(R.string.unlabeled)
                colors.add(ThemeHelper.getColor(requireContext(), ThemeHelper.COLOR_INDEX_ALL_LABELS))
                break
            }
            for (l in labels) {
                if (p.label == null) {
                    p.label = getString(R.string.unlabeled)
                    colors.add(
                        ThemeHelper.getColor(
                            requireContext(),
                            if (entries.size == 1) ThemeHelper.COLOR_INDEX_ALL_LABELS else ThemeHelper.COLOR_INDEX_UNLABELED
                        )
                    )
                    break
                } else if (p.label == l.title) {
                    colors.add(ThemeHelper.getColor(requireContext(), l.colorId))
                    break
                }
            }
        }
        val grey500 = resources.getColor(R.color.grey_500)
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLineColor = grey500
        dataSet.valueLinePart1Length = 0.175f
        dataSet.isUsingSliceColorAsValueLineColor = true
        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(grey500)
        if (pieChartShowPercentages) {
            pieChart.setUsePercentValues(true)
            data.setValueFormatter(PercentFormatter(pieChart))
        } else {
            pieChart.setUsePercentValues(false)
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return StringUtils.formatMinutes(value.toLong())
                }
            })
        }
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun refreshHistoryChart(sessions: List<Session>, color: Int) {
        val data = generateHistoryChartData(sessions, color)
        val isDurationType = statsType.selectedItemPosition == SpinnerStatsType.DURATION.ordinal
        chartHistory.apply {
            moveViewToX(data.xMax)
            this.data = data
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = if (isDurationType) 60f else 8f
            val visibleXCount = ThemeHelper.pxToDp(requireContext(), chartHistory.width)
                .toInt() / 36
            setVisibleXRangeMaximum(visibleXCount.toFloat())
            setVisibleXRangeMinimum(visibleXCount.toFloat())
            xAxis.labelCount = visibleXCount
            axisLeft.setLabelCount(5, true)
        }
        val yMax = data.yMax
        if (sessions.isNotEmpty() && yMax >= (if (isDurationType) 60f else 8f)) {
            if (isDurationType) {
                chartHistory.axisLeft.axisMaximum =
                    (ceil((yMax / 20).toDouble()) * 20).toFloat()
            } else {
                // round to the next multiple of 4
                val axisMax = if (yMax % 4 != 0f) yMax + 4 - yMax % 4 else yMax
                chartHistory.axisLeft.axisMaximum = axisMax
            }
        }

        // this part is to align the history chart to the productive time chart by setting the same width
        val p = TextPaint()
        p.textSize = resources.getDimension(R.dimen.tinyTextSize)
        val widthOfOtherChart = ThemeHelper.pxToDp(
            requireContext(), p.measureText("100%")
                .toInt()
        ).toInt()
        chartHistory.apply {
            axisLeft.minWidth = widthOfOtherChart.toFloat()
            axisLeft.maxWidth = widthOfOtherChart.toFloat()
            notifyDataSetChanged()
        }
    }

    private fun setupHistoryChart() {
        chartHistory.setXAxisRenderer(
            CustomXAxisRenderer(
                chartHistory.viewPortHandler,
                chartHistory.xAxis,
                chartHistory.getTransformer(YAxis.AxisDependency.LEFT)
            )
        )
        val yAxis = chartHistory.axisLeft
        yAxis.valueFormatter = CustomYAxisFormatter()
        yAxis.textColor = resources.getColor(R.color.grey_500)
        yAxis.textSize = resources.getDimension(R.dimen.tinyTextSize) / displayDensity
        yAxis.setDrawAxisLine(false)
        val xAxis = chartHistory.xAxis
        xAxis.textColor = resources.getColor(R.color.grey_500)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val rangeType = SpinnerRangeType.values()[rangeType.selectedItemPosition]
        xAxisFormatter = CustomXAxisFormatter(xValues, rangeType)
        xAxis.valueFormatter = xAxisFormatter
        xAxis.setAvoidFirstLastClipping(false)
        xAxis.textSize = resources.getDimension(R.dimen.tinyTextSize) / displayDensity
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.yOffset = 10f
        chartHistory.apply {
            axisLeft.gridColor = resources.getColor(R.color.transparent_dark)
            axisLeft.gridLineWidth = 1f
            extraBottomOffset = 20f
            extraLeftOffset = 10f
            axisRight.isEnabled = false
            description.isEnabled = false
            setNoDataText("")
            setHardwareAccelerationEnabled(true)
            animateY(500, Easing.EaseOutCubic)
            legend.isEnabled = false
            isDoubleTapToZoomEnabled = false
            marker = ChartMarker(requireContext(), R.layout.view_chart_marker)
            setScaleEnabled(false)
            invalidate()
            notifyDataSetChanged()
        }
    }

    private fun generateHistoryChartData(sessions: List<Session>, color: Int): LineData {
        val statsType = SpinnerStatsType.values()[statsType.selectedItemPosition]
        val rangeType = SpinnerRangeType.values()[rangeType.selectedItemPosition]
        val dummyIntervalRange = ThemeHelper.pxToDp(requireContext(), chartHistory.width).toInt() / 24
        val yValues: MutableList<Entry> = ArrayList()
        val tree = TreeMap<LocalDate, Int>()

        // generate dummy data
        val dummyEnd = LocalDate().plusDays(1)
        when (rangeType) {
            SpinnerRangeType.DAYS -> {
                val dummyBegin = dummyEnd.minusDays(dummyIntervalRange)
                var i = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusDays(1)
                }
            }
            SpinnerRangeType.WEEKS -> {
                val dummyBegin =
                    dummyEnd.minusWeeks(dummyIntervalRange).dayOfWeek().withMinimumValue()
                var i: LocalDate = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusWeeks(1)
                }
            }
            SpinnerRangeType.MONTHS -> {
                val dummyBegin = dummyEnd.minusMonths(dummyIntervalRange)
                var i: LocalDate = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusMonths(1).dayOfMonth().withMinimumValue()
                }
            }
        }

        // this is to sum up entries from the same day for visualization
        for (i in sessions.indices) {
            val localTime: LocalDate = when (rangeType) {
                SpinnerRangeType.DAYS -> LocalDate(
                    Date(
                        sessions[i].timestamp
                    )
                )
                SpinnerRangeType.WEEKS -> LocalDate(Date(sessions[i].timestamp)).dayOfWeek()
                    .withMinimumValue()
                SpinnerRangeType.MONTHS -> LocalDate(Date(sessions[i].timestamp)).dayOfMonth()
                    .withMinimumValue()
            }
            if (!tree.containsKey(localTime)) {
                tree[localTime] =
                    if (statsType == SpinnerStatsType.DURATION) sessions[i].duration else 1
            } else {
                tree[localTime] = (tree[localTime]!!
                        + if (statsType == SpinnerStatsType.DURATION) sessions[i].duration else 1)
            }
        }
        if (tree.size > 0) {
            xValues.clear()
            var i = 0
            var previousTime = tree.firstKey()
            for (crt in tree.keys) {
                // visualize intermediate days/weeks/months in case of days without completed sessions
                val beforeWhat: LocalDate = when (rangeType) {
                    SpinnerRangeType.DAYS -> crt.minusDays(1)
                    SpinnerRangeType.WEEKS -> crt.minusWeeks(1)
                    SpinnerRangeType.MONTHS -> crt.minusMonths(1)
                }
                while (previousTime.isBefore(beforeWhat)) {
                    yValues.add(Entry(i.toFloat(), 0f))
                    previousTime = when (rangeType) {
                        SpinnerRangeType.DAYS -> previousTime.plusDays(1)
                        SpinnerRangeType.WEEKS -> previousTime.plusWeeks(1)
                        SpinnerRangeType.MONTHS -> previousTime.plusMonths(1)
                    }
                    xValues.add(previousTime)
                    ++i
                }
                yValues.add(Entry(i.toFloat(), tree[crt]!!.toFloat()))
                xValues.add(crt)
                ++i
                previousTime = crt
            }
        }
        return LineData(generateLineDataSet(yValues, color))
    }

    private fun generateLineDataSet(entries: List<Entry>, color: Int): LineDataSet {
        val set = LineDataSet(entries, null)
        set.apply {
            this.color = color
            setCircleColor(color)
            setDrawFilled(true)
            fillColor = color
            lineWidth = 3f
            circleRadius = 3f
            setDrawCircleHole(false)
            disableDashedLine()
            setDrawValues(false)
            highLightColor = color
        }
        return set
    }

    private fun setupProductiveTimeChart() {
        val yAxis = chartProductiveHours.axisLeft
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return StringUtils.toPercentage(value)
            }
        }
        yAxis.textColor = resources.getColor(R.color.grey_500)
        yAxis.granularity = 0.25f
        yAxis.textSize = resources.getDimension(R.dimen.tinyTextSize) / displayDensity
        yAxis.axisMaximum = 1f
        yAxis.setDrawGridLines(true)
        yAxis.setDrawAxisLine(false)
        val xAxis = chartProductiveHours.xAxis
        xAxis.textColor = resources.getColor(R.color.grey_500)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setAvoidFirstLastClipping(false)
        xAxis.textSize = resources.getDimension(R.dimen.tinyTextSize) / displayDensity
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        chartProductiveHours.setXAxisRenderer(
            CustomXAxisRenderer(
                chartProductiveHours.viewPortHandler,
                xAxis,
                chartProductiveHours.getTransformer(YAxis.AxisDependency.LEFT)
            )
        )
        chartProductiveHours.apply {
            axisLeft.gridColor = resources.getColor(R.color.transparent_dark)
            axisLeft.gridLineWidth = 1f
            extraBottomOffset = 20f
            axisRight.isEnabled = false
            description.isEnabled = false
            setNoDataText("")
            setHardwareAccelerationEnabled(true)
            animateY(500, Easing.EaseOutCubic)
            legend.isEnabled = false
            isDoubleTapToZoomEnabled = false
            marker = ChartMarker(requireContext(), R.layout.view_chart_marker, ChartMarker.MarkerType.PERCENTAGE)
            setScaleEnabled(false)
            invalidate()
            notifyDataSetChanged()
        }
    }

    private fun generateProductiveTimeChart(
        sessions: List<Session>,
        type: SpinnerProductiveTimeType,
        color: Int
    ) {
        val yVals = ArrayList<BarEntry>()
        if (type == SpinnerProductiveTimeType.HOUR_OF_DAY) {
            val sessionsPerHour = MutableList(24) { 0L }
            // dummy values
            for (i in sessionsPerHour.indices) {
                yVals.add(BarEntry(i.toFloat(), 0f))
            }
            //TODO: use overview value(could be time) instead of hardcoded "nr of sessions" for an accurate productive chart
            val nrOfSessions = sessions.size.toLong()
            if (nrOfSessions > 0) {
                // hour of day
                for (s in sessions) {
                    val crtHourOfDay = DateTime(s.timestamp).hourOfDay
                    sessionsPerHour[crtHourOfDay] = sessionsPerHour[crtHourOfDay] + 1
                }
                for (i in sessionsPerHour.indices) {
                    yVals[i] = BarEntry(i.toFloat(), sessionsPerHour[i].toFloat() / nrOfSessions)
                }
            }
        } else if (type == SpinnerProductiveTimeType.DAY_OF_WEEK) {
            val sessionsPerDay = MutableList(7) { 0L }

            // dummy values
            for (i in sessionsPerDay.indices) {
                yVals.add(BarEntry(i.toFloat(), 0f))
            }
            val nrOfSessions = sessions.size.toLong()
            if (nrOfSessions > 0) {
                // day of week
                for (s in sessions) {
                    val crtDayOfWeek = LocalDate(s.timestamp).dayOfWeek - 1
                    sessionsPerDay[crtDayOfWeek] = sessionsPerDay[crtDayOfWeek] + 1
                }
                for (i in sessionsPerDay.indices) {
                    yVals[i] = BarEntry(i.toFloat(), sessionsPerDay[i].toFloat() / nrOfSessions)
                }
            }
        } else {
            Log.wtf(TAG, "Something went wrong in generateProductiveTimeChart")
            return
        }
        val set1 = BarDataSet(yVals, "")
        set1.color = color
        set1.highLightAlpha = 0
        set1.setDrawIcons(false)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1)
        val data = BarData(dataSets)
        data.setValueTextSize(10f)
        data.barWidth = 0.4f
        chartProductiveHours.apply {
            xAxis.valueFormatter = null
            this.data = data
            xAxis.valueFormatter = ProductiveTimeXAxisFormatter(type)
            invalidate()
            notifyDataSetChanged()
        }
    }

    companion object {
        private val TAG = StatisticsFragment::class.java.simpleName
    }
}