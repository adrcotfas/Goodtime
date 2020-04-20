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

package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.Label;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.StatisticsFragmentMainBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.card.MaterialCardView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import static com.apps.adrcotfas.goodtime.Statistics.Main.SpinnerProductiveTimeType.DAY_OF_WEEK;
import static com.apps.adrcotfas.goodtime.Statistics.Main.SpinnerProductiveTimeType.HOUR_OF_DAY;
import static com.apps.adrcotfas.goodtime.Statistics.Main.SpinnerStatsType.DURATION;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatLong;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatMinutes;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.toPercentage;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.COLOR_INDEX_ALL_LABELS;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.COLOR_INDEX_UNLABELED;

public class StatisticsFragment extends Fragment {

    private static final String TAG = StatisticsFragment.class.getSimpleName();

    //TODO: move to separate file
    private class Stats {
        long today;
        long week;
        long month;
        long total;

        Stats(long today, long week, long month, long total) {
            this.today = today;
            this.week  = week;
            this.month = month;
            this.total = total;
        }
    }

    //TODO: move to separate file and remove duplicate code
    private class StatsView {
        TextView today;
        TextView week;
        TextView month;
        TextView total;

        StatsView(TextView today, TextView week, TextView month, TextView total) {
            this.today = today;
            this.week  = week;
            this.month = month;
            this.total = total;
        }
    }

    private LiveData<List<Session>> mSessionsToObserve;
    private LineChart mChartHistory;
    private BarChart mChartProductiveHours;

    private boolean mShowPieChart = false;
    private MaterialCardView mPieChartSection;
    private PieChart mPieChart;
    private LinearLayout mPieEmptyState;

    private Spinner mStatsType;
    private Spinner mRangeType;
    private Spinner mProductiveTimeType;
    private Spinner mPieChartType;

    private TextView mHeaderOverview;
    private TextView mHeaderHistory;
    private TextView mHeaderProductiveTime;

    private CustomXAxisFormatter mXAxisFormatter;

    private List<LocalDate> xValues = new ArrayList<>();

    private StatsView mOverview;
    private StatsView mOverviewDescription;
    private LabelsViewModel mLabelsViewModel;
    private SessionViewModel mSessionViewModel;
    private LinearLayout mParentView;
    private ProgressBar mProgressBar;

    private float mDisplayDensity = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StatisticsFragmentMainBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.statistics_fragment_main, container, false);
        View view = binding.getRoot();
        mDisplayDensity = getResources().getDisplayMetrics().density;

        setHasOptionsMenu(true);

        mParentView = binding.parentLayout;
        mProgressBar = binding.progressBar;

        mChartHistory = binding.history.chart;
        mChartProductiveHours = binding.productiveHours.barChart;

        mOverview = new StatsView(
                binding.overview.todayValue,
                binding.overview.weekValue,
                binding.overview.monthValue,
                binding.overview.totalValue);

        mOverviewDescription = new StatsView(
                binding.overview.todayDescription,
                binding.overview.weekDescription,
                binding.overview.monthDescription,
                binding.overview.totalDescription
        );

        mStatsType = binding.overview.statsType;
        mRangeType = binding.history.rangeType;
        mProductiveTimeType = binding.productiveHours.timeType;
        mPieChartType = binding.pieChartSection.pieChartType;

        mHeaderOverview = binding.overview.header;
        mHeaderHistory = binding.history.headerHistory;
        mHeaderProductiveTime = binding.productiveHours.headerProductiveTime;

        mPieChartSection = binding.pieChartSection.parent;
        mPieChart = binding.pieChartSection.pieChart;
        mPieEmptyState = binding.pieChartSection.emptyState;
        setupPieChart();

        mLabelsViewModel = new ViewModelProvider(getActivity()).get(LabelsViewModel.class);
        mSessionViewModel = new ViewModelProvider(getActivity()).get(SessionViewModel.class);

        mLabelsViewModel.crtExtendedLabel.observe(getViewLifecycleOwner(), label -> refreshUi());

        setupSpinners();
        setupHistoryChart();
        setupProductiveTimeChart();

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

        return view;
    }

    private void refreshStats(List<Session> sessions) {
        final boolean isDurationType = mStatsType.getSelectedItemPosition() == DURATION.ordinal();

        final LocalDate today          = new LocalDate();
        final LocalDate thisWeekStart  = today.dayOfWeek().withMinimumValue();
        final LocalDate thisWeekEnd    = today.dayOfWeek().withMaximumValue();
        final LocalDate thisMonthStart = today.dayOfMonth().withMinimumValue();
        final LocalDate thisMonthEnd   = today.dayOfMonth().withMaximumValue();

        Stats stats = new Stats(0, 0, 0, 0);

        for (Session s : sessions) {
            final Long increment = isDurationType ? s.duration : 1L;

            final LocalDate crt = new LocalDate(new Date(s.timestamp));
            if (crt.isEqual(today)) {
                stats.today += increment;
            }
            if (crt.isAfter(thisWeekStart.minusDays(1)) && crt.isBefore(thisWeekEnd.plusDays(1))) {
                stats.week += increment;
            }
            if (crt.isAfter(thisMonthStart.minusDays(1)) && crt.isBefore(thisMonthEnd.plusDays(1))) {
                stats.month += increment;
            }
            if (isDurationType) {
                stats.total += increment;
            }
        }
        if (!isDurationType) {
            stats.total += sessions.size();
        }

        mOverview.today.setText(isDurationType
                ? formatMinutes(stats.today)
                : formatLong(stats.today));
        mOverview.week.setText(isDurationType
                ? formatMinutes(stats.week)
                : formatLong(stats.week));
        mOverview.month.setText(isDurationType
                ? formatMinutes(stats.month)
                : formatLong(stats.month));
        mOverview.total.setText(isDurationType
                ? formatMinutes(stats.total)
                : formatLong(stats.total));

        mOverviewDescription.week.setText(
                getResources().getString(R.string.statistics_week) + " " + thisWeekStart.getWeekOfWeekyear());
        StringBuilder sb = new StringBuilder(thisMonthEnd.toString("MMMM"));
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        mOverviewDescription.month.setText(sb.toString());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> statsTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_stats_type, R.layout.spinner_item);
        statsTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mStatsType.setAdapter(statsTypeAdapter);
        mStatsType.setSelection(0, false);
        mStatsType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                refreshUi();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ArrayAdapter <CharSequence> rangeTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_range_type, R.layout.spinner_item);
        rangeTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mRangeType.setAdapter(rangeTypeAdapter);
        mRangeType.setSelection(0, false);
        mRangeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mXAxisFormatter.setRangeType(SpinnerRangeType.values()[position]);
                refreshUi();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ArrayAdapter <CharSequence> timeTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_productive_time_type, R.layout.spinner_item);
        timeTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mProductiveTimeType.setAdapter(timeTypeAdapter);
        mProductiveTimeType.setSelection(0, false);
        mProductiveTimeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                refreshUi();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ArrayAdapter <CharSequence> pieTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_pie_time_type, R.layout.spinner_item);
        pieTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mPieChartType.setAdapter(pieTypeAdapter);
        mPieChartType.setSelection(3, false);
        mPieChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshUi();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshProductiveTimeChart(List<Session> sessions, int color) {
        // generate according to spinner
        if (mProductiveTimeType.getSelectedItemPosition() == HOUR_OF_DAY.ordinal()) {
            generateProductiveTimeChart(sessions, HOUR_OF_DAY, color);

            final int visibleXCount = (int) ThemeHelper.pxToDp(getContext(), mChartHistory.getWidth()) / 36;
            mChartProductiveHours.setVisibleXRangeMaximum(visibleXCount);
            mChartProductiveHours.setVisibleXRangeMinimum(visibleXCount);
            mChartProductiveHours.getXAxis().setLabelCount(visibleXCount);
        } else {
            generateProductiveTimeChart(sessions, DAY_OF_WEEK, color);

            mChartProductiveHours.setVisibleXRangeMaximum(7);
            mChartProductiveHours.setVisibleXRangeMinimum(7);
            mChartProductiveHours.getXAxis().setLabelCount(7);
        }

        mChartProductiveHours.getBarData().setDrawValues(false);
        IBarDataSet b = mChartProductiveHours.getData().getDataSets().get(0);

        float maxY = 0;
        int maxIdx = 0;
        for (int i = 0; i <  b.getEntryCount(); ++i) {
            float crtY = b.getEntryForIndex(i).getY();
            if (crtY > maxY) {
                maxY = crtY;
                maxIdx = i;
            }
        }

        mChartProductiveHours.moveViewToX(maxIdx);
        mChartProductiveHours.invalidate();
        mChartProductiveHours.notifyDataSetChanged();
    }

    //TODO: make more efficient when setting spinners to not refresh all of it if not needed
    private void refreshUi() {
        final Label label = mLabelsViewModel.crtExtendedLabel.getValue();

        if (label != null) {
            if (mSessionsToObserve != null) {
                mSessionsToObserve.removeObservers(this);
            }

            final int color = ThemeHelper.getColor(getActivity(), label.colorId);
            mOverview.today.setTextColor(color);
            mOverview.week.setTextColor(color);
            mOverview.month.setTextColor(color);
            mOverview.total.setTextColor(color);

            mHeaderOverview.setTextColor(color);
            mHeaderHistory.setTextColor(color);
            mHeaderProductiveTime.setTextColor(color);

            String s = label.title;
            if (getString(R.string.label_all).equals(s)) {
                mSessionsToObserve = mSessionViewModel.getAllSessionsByEndTime();
                mShowPieChart = true;
            } else if ("unlabeled".equals(s)) {
                mSessionsToObserve = mSessionViewModel.getAllSessionsUnlabeled();
                mShowPieChart = false;
            } else {
                mSessionsToObserve = mSessionViewModel.getSessions(label.title);
                mShowPieChart = false;
            }
            mSessionsToObserve.observe(getViewLifecycleOwner(), sessions -> {
                refreshStats(sessions);
                refreshHistoryChart(sessions, color);
                refreshProductiveTimeChart(sessions, color);
                mPieChartSection.setVisibility(mShowPieChart ? View.VISIBLE : View.GONE);
                if (mShowPieChart){
                    LiveData<List<Label>> labelsLd = mLabelsViewModel.getLabels();
                    labelsLd.observe(getViewLifecycleOwner(), labels -> {
                        labelsLd.removeObservers(requireActivity());
                        refreshPieChart(sessions, labels);
                    });
                }

                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    mProgressBar.setVisibility(View.GONE);
                    mParentView.setVisibility(View.VISIBLE);
                }, 200);
            });
        }
    }

    private void setupPieChart() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        @ColorInt int color = typedValue.data;
        mPieChart.setHoleColor(color);

        mPieChart.getLegend().setEnabled(false);
        mPieChart.setUsePercentValues(true);
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleRadius(80);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(8, 8, 8, 8);
        mPieChart.highlightValues(null);
        mPieChart.setEntryLabelColor(getResources().getColor(R.color.grey_500));
        mPieChart.setEntryLabelTextSize(11f);
        mPieChart.setTransparentCircleRadius(0);
        mPieChart.setDragDecelerationFrictionCoef(0.95f);
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(false);
        mPieChart.setNoDataText("");
    }

    private void refreshPieChart(List<Session> sessions, List<Label> labels) {
        Map<String, Integer> totalTimePerLabel = new HashMap<>();

        final PieStatsType pieStatsType =
                PieStatsType.values()[mPieChartType.getSelectedItemPosition()];

        final LocalDate today = new LocalDate();
        final long todayStart = today.toDateTimeAtStartOfDay().getMillis();
        final long thisWeekStart  = today.dayOfWeek().withMinimumValue().toDateTimeAtStartOfDay().getMillis();
        final long thisMonthStart = today.dayOfMonth().withMinimumValue().toDateTimeAtStartOfDay().getMillis();

        List<Session> filteredSessions = new ArrayList<>(sessions);
        switch (pieStatsType) {
            case TODAY:
                for (Session s : sessions) {
                    if (s.timestamp < todayStart) {
                        filteredSessions.remove(s);
                    }
                }
                break;
            case THIS_WEEK:
                for (Session s : sessions) {
                    if (s.timestamp < thisWeekStart) {
                        filteredSessions.remove(s);
                    }
                }
                break;
            case THIS_MONTH:
                for (Session s : sessions) {
                    if (s.timestamp < thisMonthStart) {
                        filteredSessions.remove(s);
                    }
                }
                break;
            case TOTAL:
                break;
        }

        if (filteredSessions.isEmpty()) {
            mPieEmptyState.setVisibility(View.VISIBLE);
            mPieChart.setVisibility(View.GONE);
            return;
        } else {
            mPieEmptyState.setVisibility(View.GONE);
            mPieChart.setVisibility(View.VISIBLE);
        }

        for (Session s : filteredSessions) {
            if (!s.archived) {
                if (totalTimePerLabel.containsKey(s.label)) {
                    totalTimePerLabel.put(s.label, totalTimePerLabel.get(s.label) + s.duration);
                } else {
                    totalTimePerLabel.put(s.label, s.duration);
                }
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (String label : totalTimePerLabel.keySet()) {
            entries.add(new PieEntry(totalTimePerLabel.get(label), label));
        }

        Collections.sort(entries, (o1, o2) -> Float.compare(o2.getValue(), o1.getValue()));

        ArrayList<Integer> colors = new ArrayList<>();
        for (PieEntry p : entries) {
            if (labels.isEmpty()) {
                p.setLabel(getString(R.string.unlabeled));
                colors.add(ThemeHelper.getColor(getContext(), COLOR_INDEX_ALL_LABELS));
                break;
            }
            for (Label l : labels) {
                if (p.getLabel() == null) {
                    p.setLabel(getString(R.string.unlabeled));
                    colors.add(ThemeHelper.getColor(getContext(), entries.size() == 1
                            ? COLOR_INDEX_ALL_LABELS : COLOR_INDEX_UNLABELED));
                    break;
                } else if (p.getLabel().equals(l.title)) {
                    colors.add(ThemeHelper.getColor(getContext(), l.colorId));
                    break;
                }
            }
        }

        int grey500 = getResources().getColor(R.color.grey_500);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineColor(grey500);
        dataSet.setValueLinePart1Length(0.175f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(grey500);

        mPieChart.setData(data);
        mPieChart.invalidate();
    }

    private void refreshHistoryChart(List<Session> sessions, int color) {

        final LineData data = generateHistoryChartData(sessions, color);
        final boolean isDurationType = mStatsType.getSelectedItemPosition() == DURATION.ordinal();

        mChartHistory.moveViewToX(data.getXMax());
        mChartHistory.setData(data);
        mChartHistory.getData().setHighlightEnabled(false);

        mChartHistory.getAxisLeft().setAxisMinimum(0f);
        mChartHistory.getAxisLeft().setAxisMaximum(isDurationType ? 60f : 8f);

        final int visibleXCount = (int) ThemeHelper.pxToDp(requireContext(), mChartHistory.getWidth()) / 36;
        mChartHistory.setVisibleXRangeMaximum(visibleXCount);
        mChartHistory.setVisibleXRangeMinimum(visibleXCount);
        mChartHistory.getXAxis().setLabelCount(visibleXCount);
        mChartHistory.getAxisLeft().setLabelCount(5, true);

        final float yMax = data.getYMax();
        if (sessions.size() > 0 && yMax >= (isDurationType ? 60 : 8f)) {
            if (isDurationType) {
                mChartHistory.getAxisLeft().setAxisMaximum((float) (Math.ceil((double)(yMax / 20)) * 20));
            } else {
                // round to the next multiple of 4
                float axisMax = (yMax % 4 != 0) ? yMax + 4 - (yMax % 4) : yMax;
                mChartHistory.getAxisLeft().setAxisMaximum(axisMax);
            }
        }

        // this part is to align the history chart to the productive time chart by setting the same width
        TextPaint p = new TextPaint();
        p.setTextSize(getResources().getDimension(R.dimen.tinyTextSize));
        int widthOfOtherChart = (int) ThemeHelper.pxToDp(getContext(), (int) p.measureText("100%"));
        mChartHistory.getAxisLeft().setMinWidth(widthOfOtherChart);
        mChartHistory.getAxisLeft().setMaxWidth(widthOfOtherChart);
        mChartHistory.notifyDataSetChanged();
    }

    private void setupHistoryChart() {
        mChartHistory.setXAxisRenderer(new CustomXAxisRenderer(
                mChartHistory.getViewPortHandler(),
                mChartHistory.getXAxis(),
                mChartHistory.getTransformer(YAxis.AxisDependency.LEFT)));

        YAxis yAxis = mChartHistory.getAxisLeft();
        yAxis.setValueFormatter(new CustomYAxisFormatter());
        yAxis.setTextColor(getResources().getColor(R.color.grey_500));
        yAxis.setTextSize(getResources().getDimension(R.dimen.tinyTextSize) / mDisplayDensity);
        yAxis.setDrawAxisLine(false);

        XAxis xAxis = mChartHistory.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.grey_500));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        final SpinnerRangeType rangeType =
                SpinnerRangeType.values()[mRangeType.getSelectedItemPosition()];

        mXAxisFormatter = new CustomXAxisFormatter(xValues, rangeType);
        xAxis.setValueFormatter(mXAxisFormatter);
        xAxis.setAvoidFirstLastClipping(false);
        xAxis.setTextSize(getResources().getDimension(R.dimen.tinyTextSize) / mDisplayDensity);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        xAxis.setYOffset(10f);

        mChartHistory.getAxisLeft().setGridColor(getResources().getColor(R.color.transparent_dark));
        mChartHistory.getAxisLeft().setGridLineWidth(1);

        mChartHistory.setExtraBottomOffset(20f);
        mChartHistory.setExtraLeftOffset(10f);
        mChartHistory.getAxisRight().setEnabled(false);
        mChartHistory.getDescription().setEnabled(false);
        mChartHistory.setNoDataText("");
        mChartHistory.setHardwareAccelerationEnabled(true);
        mChartHistory.animateY(500, Easing.EasingOption.EaseOutCubic);
        mChartHistory.getLegend().setEnabled(false);
        mChartHistory.setDoubleTapToZoomEnabled(false);
        mChartHistory.setScaleEnabled(false);
        mChartHistory.invalidate();
        mChartHistory.notifyDataSetChanged();
    }

    private LineData generateHistoryChartData(List<Session> sessions, int color) {

        final SpinnerStatsType statsType =
                SpinnerStatsType.values()[mStatsType.getSelectedItemPosition()];
        final SpinnerRangeType rangeType =
                SpinnerRangeType.values()[mRangeType.getSelectedItemPosition()];

        final int DUMMY_INTERVAL_RANGE = (int) ThemeHelper.pxToDp(getContext(), mChartHistory.getWidth()) / 24;

        List<Entry> yVals = new ArrayList<>();
        TreeMap<LocalDate, Integer> tree = new TreeMap<>();

        // generate dummy data
        LocalDate dummyEnd = new LocalDate().plusDays(1);
        switch (rangeType) {
            case DAYS:
                LocalDate dummyBegin = dummyEnd.minusDays(DUMMY_INTERVAL_RANGE);
                for (LocalDate i = dummyBegin; i.isBefore(dummyEnd); i = i.plusDays(1)) {
                    tree.put(i, 0);
                }
                break;
            case WEEKS:
                dummyBegin = dummyEnd.minusWeeks(DUMMY_INTERVAL_RANGE).dayOfWeek().withMinimumValue();
                for (LocalDate i = dummyBegin; i.isBefore(dummyEnd); i = i.plusWeeks(1)) {
                    tree.put(i, 0);
                }
                break;
            case MONTHS:
                dummyBegin = dummyEnd.minusMonths(DUMMY_INTERVAL_RANGE);
                for (LocalDate i = dummyBegin; i.isBefore(dummyEnd); i = i.plusMonths(1).dayOfMonth().withMinimumValue()) {
                    tree.put(i, 0);
                }
                break;
        }

        // this is to sum up entries from the same day for visualization
        for (int i = 0; i < sessions.size(); ++i) {
            LocalDate localTime = new LocalDate();
            switch (rangeType) {
                case DAYS:
                    localTime = new LocalDate(new Date(sessions.get(i).timestamp));
                    break;
                case WEEKS:
                    localTime = new LocalDate(new Date(sessions.get(i).timestamp)).dayOfWeek().withMinimumValue();
                    break;
                case MONTHS:
                    localTime = new LocalDate(new Date(sessions.get(i).timestamp)).dayOfMonth().withMinimumValue();
                    break;
            }

            if (!tree.containsKey(localTime)) {
                tree.put(localTime, statsType == DURATION ? sessions.get(i).duration : 1);
            } else {
                tree.put(localTime, tree.get(localTime)
                        + (statsType == DURATION ? sessions.get(i).duration : 1));
            }
        }

        if (tree.size() > 0) {
            xValues.clear();
            int i = 0;
            LocalDate previousTime = tree.firstKey();

            for (LocalDate crt : tree.keySet()) {
                // visualize intermediate days/weeks/months in case of days without completed sessions
                LocalDate beforeWhat = new LocalDate();
                switch (rangeType) {
                    case DAYS:
                        beforeWhat = crt.minusDays(1);
                        break;
                    case WEEKS:
                        beforeWhat = crt.minusWeeks(1);
                        break;
                    case MONTHS:
                        beforeWhat = crt.minusMonths(1);
                }

                while(previousTime.isBefore(beforeWhat)) {
                    yVals.add(new Entry(i, 0));

                    switch (rangeType) {
                        case DAYS:
                            previousTime = previousTime.plusDays(1);
                            break;
                        case WEEKS:
                            previousTime = previousTime.plusWeeks(1);
                            break;
                        case MONTHS:
                            previousTime = previousTime.plusMonths(1);
                    }
                    xValues.add(previousTime);
                    ++i;
                }
                yVals.add(new Entry(i, tree.get(crt)));
                xValues.add(crt);
                ++i;
                previousTime = crt;
            }
        }
        return new LineData(generateLineDataSet(yVals, color));
    }

    private LineDataSet generateLineDataSet(List<Entry> entries, int color) {

        LineDataSet set = new LineDataSet(entries, null);
        set.setColor(color);
        set.setCircleColor(color);
        set.setDrawFilled(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT);
        set.setFillColor(color);
        set.setLineWidth(3f);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.disableDashedLine();
        set.setDrawValues(false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            set.setDrawFilled(false);
            set.setLineWidth(2f);
            set.setCircleRadius(4f);
            set.setDrawCircleHole(true);
        }
        return set;
    }

    private void setupProductiveTimeChart() {
        YAxis yAxis = mChartProductiveHours.getAxisLeft();
        yAxis.setValueFormatter((value, axis) -> toPercentage(value));
        yAxis.setTextColor(getResources().getColor(R.color.grey_500));
        yAxis.setGranularity(0.25f);
        yAxis.setTextSize(getResources().getDimension(R.dimen.tinyTextSize) / mDisplayDensity);
        yAxis.setAxisMaximum(1.F);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawAxisLine(false);

        XAxis xAxis = mChartProductiveHours.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.grey_500));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setAvoidFirstLastClipping(false);
        xAxis.setTextSize(getResources().getDimension(R.dimen.tinyTextSize) / mDisplayDensity);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        mChartProductiveHours.setXAxisRenderer(new CustomXAxisRenderer(
                mChartProductiveHours.getViewPortHandler(),
                xAxis,
                mChartProductiveHours.getTransformer(YAxis.AxisDependency.LEFT)));

        mChartProductiveHours.getAxisLeft().setGridColor(getResources().getColor(R.color.transparent_dark));
        mChartProductiveHours.getAxisLeft().setGridLineWidth(1);


        mChartProductiveHours.setExtraBottomOffset(20f);
        mChartProductiveHours.getAxisRight().setEnabled(false);
        mChartProductiveHours.getDescription().setEnabled(false);
        mChartProductiveHours.setNoDataText("");
        mChartProductiveHours.setHardwareAccelerationEnabled(true);
        mChartProductiveHours.animateY(500, Easing.EasingOption.EaseOutCubic);
        mChartProductiveHours.getLegend().setEnabled(false);
        mChartProductiveHours.setDoubleTapToZoomEnabled(false);
        mChartProductiveHours.setScaleEnabled(false);
        mChartProductiveHours.invalidate();
        mChartProductiveHours.notifyDataSetChanged();
    }

    private void generateProductiveTimeChart(List<Session> sessions, SpinnerProductiveTimeType type, int color) {
        ArrayList<BarEntry> yVals = new ArrayList<>();
        if (type == HOUR_OF_DAY) {
            List<Long> sessionsPerHour = Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
            // dummy values
            for (int i = 0; i < sessionsPerHour.size(); ++i) {
                yVals.add(new BarEntry(i, 0.F));
            }

            final long nrOfSessions = sessions.size();
            if (nrOfSessions > 0) {
                // hour of day
                for (Session s : sessions) {
                    int crtHourOfDay = new DateTime(s.timestamp).getHourOfDay();
                    sessionsPerHour.set(crtHourOfDay, sessionsPerHour.get(crtHourOfDay) + 1);
                }

                for (int i = 0; i < sessionsPerHour.size(); ++i) {
                    yVals.set(i, new BarEntry(i, ((float)sessionsPerHour.get(i) / nrOfSessions)));
                }
            }
        } else if (type == DAY_OF_WEEK){
            List<Long> sessionsPerDay = Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L);

            // dummy values
            for (int i = 0; i < sessionsPerDay.size(); ++i) {
                yVals.add(new BarEntry(i, 0.F));
            }

            final long nrOfSessions = sessions.size();
            if (nrOfSessions > 0) {
                // day of week
                for (Session s : sessions) {
                    int crtDayOfWeek = new LocalDate(s.timestamp).getDayOfWeek() - 1;
                    sessionsPerDay.set(crtDayOfWeek, sessionsPerDay.get(crtDayOfWeek) + 1);
                }

                for (int i = 0; i < sessionsPerDay.size(); ++i) {
                    yVals.set(i, new BarEntry(i, ((float)sessionsPerDay.get(i) / nrOfSessions)));
                }
            }
        } else {
            Log.wtf(TAG, "Something went wrong in generateProductiveTimeChart");
            return;
        }

        BarDataSet set1 = new BarDataSet(yVals, "");
        set1.setColor(color);
        set1.setHighLightAlpha(0);
        set1.setDrawIcons(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.4f);
        mChartProductiveHours.getXAxis().setValueFormatter(null);
        mChartProductiveHours.setData(data);
        mChartProductiveHours.getXAxis().setValueFormatter(new ProductiveTimeXAxisFormatter(type));
        mChartProductiveHours.invalidate();
        mChartProductiveHours.notifyDataSetChanged();
    }
}
