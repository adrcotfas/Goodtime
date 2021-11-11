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

import android.annotation.SuppressLint
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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import android.view.MotionEvent
import com.github.mikephil.charting.formatter.PercentFormatter
import android.text.TextPaint
import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentMainBinding
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.util.*
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.animation.Easing
import com.apps.adrcotfas.goodtime.util.StringUtils.formatLong
import com.apps.adrcotfas.goodtime.util.StringUtils.formatMinutes
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil

@AndroidEntryPoint
class StatisticsFragment : Fragment() {
    private class Stats(
        var today: Long,
        var week: Long,
        var month: Long,
        var total: Long
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
    private lateinit var xAxisFormatter: DayWeekMonthXAxisFormatter
    private val xValues: MutableList<LocalDate> = ArrayList()

    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val labelsViewModel: LabelsViewModel by activityViewModels()

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    private lateinit var parentView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var displayDensity = 1f

    private lateinit var binding: StatisticsFragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.statistics_fragment_main, container, false)

        val view = binding.root
        displayDensity = resources.displayMetrics.density
        setHasOptionsMenu(true)
        parentView = binding.parentLayout
        progressBar = binding.progressBar
        chartHistory = binding.history.chart
        chartProductiveHours = binding.productiveHours.barChart
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

    private fun calculateOverviewStats(sessions: List<Session>, isDurationType: Boolean): Stats {
        val today = LocalDate.now()
        val thisWeekStart: LocalDate =
            today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek()))
        val thisWeekEnd: LocalDate = today.with(TemporalAdjusters.nextOrSame(lastDayOfWeek()))
        val thisMonthStart: LocalDate = today.with(TemporalAdjusters.firstDayOfMonth())
        val thisMonthEnd: LocalDate = today.with(TemporalAdjusters.lastDayOfMonth())
        val stats = Stats(0, 0, 0, 0)
        for (s in sessions) {
            val crt =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(s.timestamp), ZoneId.systemDefault())
                    .toLocalDate()
            val increment = if (isDurationType) s.duration.toLong() else 1L
            if (crt.isEqual(today)) {
                stats.today += increment
            }
            if ((crt >= thisWeekStart) && (crt <= thisWeekEnd)) {
                stats.week += increment
            }
            if ((crt >= thisMonthStart) && (crt <= thisMonthEnd)) {
                stats.month += increment
            }
            if (isDurationType) {
                stats.total += increment
            }
        }

        if (!isDurationType) {
            stats.total += sessions.size.toLong()
        }
        return stats
    }

    /**
     * @param value minutes or number of sessions, depending on isDurationType
     * @param isDurationType specifies if the value is in seconds or number of sessions
     */
    private fun formatMinutesOrNumSessionsToOverviewTime(
        value: Long,
        isDurationType: Boolean
    ): String {
        return if (isDurationType) {
            formatMinutes(value)
        } else {
            formatLong(value)
        }
    }

    private fun getThisWeekNumber() =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDayOfWeek())).get(
            ChronoField.ALIGNED_WEEK_OF_YEAR
        )

    private fun getCurrentMonthString(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"))

    @SuppressLint("SetTextI18n")
    private fun refreshStats(sessions: List<Session>) {
        val isDurationType = statsType.selectedItemPosition == SpinnerStatsType.DURATION.ordinal
        val stats = calculateOverviewStats(sessions, isDurationType)

        val overview = binding.overview
        overview.apply {
            todayValue.text = formatMinutesOrNumSessionsToOverviewTime(stats.today, isDurationType)
            weekValue.text = formatMinutesOrNumSessionsToOverviewTime(stats.week, isDurationType)
            monthValue.text = formatMinutesOrNumSessionsToOverviewTime(stats.month, isDurationType)
            totalValue.text = formatMinutesOrNumSessionsToOverviewTime(stats.total, isDurationType)
            weekDescription.text =
                "${resources.getString(R.string.statistics_week)} ${getThisWeekNumber()}"
            monthDescription.text = getCurrentMonthString()
        }
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
                    xAxisFormatter.setRangeType(HistorySpinnerRangeType.values()[position])
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
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
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
        val productiveTimeType =
            ProductiveTimeType.values()[productiveTimeType.selectedItemPosition]
        val visibleXCount = if (productiveTimeType == ProductiveTimeType.HOUR_OF_DAY) {
            ThemeHelper.pxToDp(requireContext(), chartHistory.width).toInt() / 36
        } else {
            DayOfWeek.values().size
        }

        generateProductiveTimeChart(sessions, productiveTimeType, color)
        chartProductiveHours.apply {
            setVisibleXRangeMaximum(visibleXCount.toFloat())
            setVisibleXRangeMinimum(visibleXCount.toFloat())
            xAxis.labelCount = visibleXCount
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
            highlightValue(null) // clear any marker
        }
    }

    //TODO: make more efficient when setting spinners to not refresh all of it if not needed
    private fun refreshUi() {
        val label = labelsViewModel.crtExtendedLabel.value
        if (label != null) {
            sessionsToObserve?.removeObservers(this)
            val color = ThemeHelper.getColor(requireContext(), label.colorId)
            binding.overview.apply {
                todayValue.setTextColor(color)
                weekValue.setTextColor(color)
                monthValue.setTextColor(color)
                totalValue.setTextColor(color)
            }
            headerOverview.setTextColor(color)
            headerHistory.setTextColor(color)
            headerProductiveTime.setTextColor(color)
            when (label.title) {
                getString(R.string.label_all) -> {
                    sessionsToObserve = sessionViewModel.allSessionsUnarchived
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

                // Adjust the finished sessions according to the configured workday start
                sessions.forEach {
                    it.timestamp -= preferenceHelper.getStartOfDayDeltaMillis()
                }

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
            setEntryLabelColor(resources.getColor(R.color.grey500))
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

        val today = LocalDate.now()
        val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toLocalDate()
        val thisWeekStart: LocalDate =
            today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek()))
        val thisMonthStart: LocalDate = today.with(TemporalAdjusters.firstDayOfMonth())

        val filteredSessions: MutableList<Session> = ArrayList(sessions)
        when (pieStatsType) {
            PieStatsType.TODAY -> for (s in sessions) {
                if (s.timestamp.toLocalDate() < todayStart) {
                    filteredSessions.remove(s)
                }
            }
            PieStatsType.THIS_WEEK -> for (s in sessions) {
                if (s.timestamp.toLocalDate() < thisWeekStart) {
                    filteredSessions.remove(s)
                }
            }
            PieStatsType.THIS_MONTH -> for (s in sessions) {
                if (s.timestamp.toLocalDate() < thisMonthStart) {
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
                colors.add(
                    ThemeHelper.getColor(
                        requireContext(),
                        ThemeHelper.COLOR_INDEX_ALL_LABELS
                    )
                )
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
        val grey500 = resources.getColor(R.color.grey500)
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
                    return formatMinutes(value.toLong())
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
            marker = ChartMarker(requireContext(), R.layout.view_chart_marker, if (isDurationType) ChartMarker.MarkerType.MINUTES else ChartMarker.MarkerType.INTEGER)
            highlightValue(null) // clear marker selection
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
        yAxis.textColor = resources.getColor(R.color.grey500)
        yAxis.textSize = resources.getDimension(R.dimen.tinyTextSize) / displayDensity
        yAxis.setDrawAxisLine(false)
        val xAxis = chartHistory.xAxis
        xAxis.textColor = resources.getColor(R.color.grey500)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val rangeType = HistorySpinnerRangeType.values()[rangeType.selectedItemPosition]
        xAxisFormatter = DayWeekMonthXAxisFormatter(xValues, rangeType)
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
            marker = ChartMarker(requireContext(), R.layout.view_chart_marker, ChartMarker.MarkerType.MINUTES)
            setScaleEnabled(false)
            invalidate()
            notifyDataSetChanged()
        }
    }

    private fun generateHistoryChartData(sessions: List<Session>, color: Int): LineData {
        val statsType = SpinnerStatsType.values()[statsType.selectedItemPosition]
        val rangeType = HistorySpinnerRangeType.values()[rangeType.selectedItemPosition]
        val dummyIntervalRange =
            ThemeHelper.pxToDp(requireContext(), chartHistory.width).toLong() / 24L
        val yValues: MutableList<Entry> = ArrayList()
        val tree = TreeMap<LocalDate, Int>()

        // generate dummy data
        val dummyEnd = LocalDate.now().plusDays(1)
        when (rangeType) {
            HistorySpinnerRangeType.DAYS -> {
                val dummyBegin = dummyEnd.minusDays(dummyIntervalRange)
                var i = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusDays(1)
                }
            }
            HistorySpinnerRangeType.WEEKS -> {
                val dummyBegin = dummyEnd.minusWeeks(dummyIntervalRange).with(
                    TemporalAdjusters.firstInMonth(firstDayOfWeek())
                )
                var i: LocalDate = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusWeeks(1)
                }
            }
            HistorySpinnerRangeType.MONTHS -> {
                val dummyBegin = dummyEnd.minusMonths(dummyIntervalRange).withDayOfMonth(1)
                var i: LocalDate = dummyBegin
                while (i.isBefore(dummyEnd)) {
                    tree[i] = 0
                    i = i.plusMonths(1).withDayOfMonth(1)
                }
            }
        }

        val isDurationType = statsType == SpinnerStatsType.DURATION
        // this is to sum up entries from the same day for visualization
        for (i in sessions.indices) {
            val millis = sessions[i].timestamp
            val localDate =
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            val localTime = when (rangeType) {
                HistorySpinnerRangeType.DAYS ->
                    localDate
                HistorySpinnerRangeType.WEEKS ->
                    localDate.with(
                        TemporalAdjusters.previousOrSame(firstDayOfWeek())
                    )

                HistorySpinnerRangeType.MONTHS ->
                    localDate.with(TemporalAdjusters.firstDayOfMonth())
            }
            if (!tree.containsKey(localTime)) {
                tree[localTime] = if (isDurationType) sessions[i].duration else 1
            } else {
                tree[localTime] =
                    (tree[localTime]!! + if (isDurationType) sessions[i].duration else 1)
            }
        }
        if (tree.size > 0) {
            xValues.clear()
            var i = 0
            var previousTime = tree.firstKey()
            for (crt in tree.keys) {
                // visualize intermediate days/weeks/months in case of days without completed sessions
                val beforeWhat: LocalDate = when (rangeType) {
                    HistorySpinnerRangeType.DAYS -> crt.minusDays(1)
                    HistorySpinnerRangeType.WEEKS -> crt.minusWeeks(1)
                    HistorySpinnerRangeType.MONTHS -> crt.minusMonths(1)
                }
                while (previousTime.isBefore(beforeWhat)) {
                    yValues.add(Entry(i.toFloat(), 0f))
                    previousTime = when (rangeType) {
                        HistorySpinnerRangeType.DAYS -> previousTime.plusDays(1)
                        HistorySpinnerRangeType.WEEKS -> previousTime.plusWeeks(1)
                        HistorySpinnerRangeType.MONTHS -> previousTime.plusMonths(1)
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
            highlightLineWidth = 0.0000001f // for some reason 0f does not do the trick
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
        yAxis.textColor = resources.getColor(R.color.grey500)
        yAxis.granularity = 0.25f
        yAxis.textSize = resources.getDimension(R.dimen.tinyTextSize) / displayDensity
        yAxis.axisMaximum = 1f
        yAxis.setDrawGridLines(true)
        yAxis.setDrawAxisLine(false)
        val xAxis = chartProductiveHours.xAxis
        xAxis.textColor = resources.getColor(R.color.grey500)
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
            marker = ChartMarker(
                requireContext(),
                R.layout.view_chart_marker,
                ChartMarker.MarkerType.PERCENTAGE
            )
            setScaleEnabled(false)
            //TODO: do we need these here?
            invalidate()
            notifyDataSetChanged()
        }
    }

    private fun generateProductiveTimeChart(
        sessions: List<Session>,
        type: ProductiveTimeType,
        color: Int
    ) {
        val values = ArrayList<BarEntry>()
        val productiveTimeType = SpinnerStatsType.values()[statsType.selectedItemPosition]
        val sessionsPerProductiveTimeType =
            MutableList(if (type == ProductiveTimeType.HOUR_OF_DAY) 24 else DayOfWeek.values().size) { 0L }
        for (i in sessionsPerProductiveTimeType.indices) {
            values.add(BarEntry(i.toFloat(), 0f))
        }

        if (productiveTimeType == SpinnerStatsType.DURATION) {
            var totalTime = 0L
            for (s in sessions) {
                // When showing hours, re-adjust the displayed values to the real values
                if (type == ProductiveTimeType.HOUR_OF_DAY) {
                    s.timestamp += preferenceHelper.getStartOfDayDeltaMillis()
                }
                totalTime += s.duration
                val crtIndex =
                    if (type == ProductiveTimeType.HOUR_OF_DAY) s.timestamp.toLocalTime().hour
                    else s.timestamp.toLocalDateTime().dayOfWeek.value - 1
                sessionsPerProductiveTimeType[crtIndex] =
                    sessionsPerProductiveTimeType[crtIndex] + s.duration
            }
            for (i in sessionsPerProductiveTimeType.indices) {
                values[i] =
                    BarEntry(i.toFloat(), if (totalTime == 0L) 0f else sessionsPerProductiveTimeType[i].toFloat() / totalTime)
            }

        } else if (productiveTimeType == SpinnerStatsType.NR_OF_SESSIONS) {
            for (s in sessions) {
                // When showing hours, re-adjust the displayed values to the real values
                if (type == ProductiveTimeType.HOUR_OF_DAY) {
                    s.timestamp -= preferenceHelper.getStartOfDayDeltaMillis()
                }
                val crtIndex =
                    if (type == ProductiveTimeType.HOUR_OF_DAY) s.timestamp.toLocalTime().hour
                    else s.timestamp.toLocalDateTime().dayOfWeek.value - 1
                ++sessionsPerProductiveTimeType[crtIndex]
            }
            for (i in sessionsPerProductiveTimeType.indices) {
                values[i] = BarEntry(
                    i.toFloat(),
                    sessionsPerProductiveTimeType[i].toFloat() / sessions.size
                )
            }
        }

        val set1 = BarDataSet(values, "")
        set1.color = color
        set1.highLightAlpha = 0
        set1.setDrawIcons(false)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1)
        val data = BarData(dataSets)
        data.setValueTextSize(10f)
        data.barWidth = 0.4f
        chartProductiveHours.apply {
            this.data = data
            xAxis.valueFormatter =
                ProductiveTimeXAxisFormatter(type, DateFormat.is24HourFormat(requireContext()))
            invalidate()
            notifyDataSetChanged()
        }
    }

    companion object {
        private val TAG = StatisticsFragment::class.java.simpleName
    }
}