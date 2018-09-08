package com.apps.adrcotfas.goodtime.Statistics;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.databinding.StatisticsMainBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import static com.apps.adrcotfas.goodtime.Statistics.SpinnerStatsType.DURATION;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatMinutes;

public class StatisticsFragment extends Fragment {

    private LineChart mChart;

    private TextView mStatsToday;
    private TextView mStatsThisWeek;
    private TextView mStatsThisMonth;
    private TextView mStatsTotal;

    private Spinner mStatsTypeSpinner;
    private Spinner mRangeTypeSpinner;

    private CustomXAxisFormatter mXAxisFormatter;

    final private float CHART_TEXT_SIZE = 12f;

    private List<LocalDate> xValues = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StatisticsMainBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.statistics_main, container, false);
        View view = binding.getRoot();

        mChart = binding.chart;
        mStatsToday = binding.statsToday;
        mStatsThisWeek = binding.statsWeek;
        mStatsThisMonth = binding.statsMonth;
        mStatsTotal = binding.statsTotal;

        mStatsTypeSpinner = binding.statsTypeSpinner;
        mRangeTypeSpinner = binding.rangeTypeSpinner;
        binding.allEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AllEntriesActivity.class);
                startActivity(intent);
            }
        });

        setupSpinners();
        setupChart();
        setupSessionsObserver();
        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> statsTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_stats_type, android.R.layout.simple_spinner_item);
        statsTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatsTypeSpinner.setAdapter(statsTypeAdapter);

        mStatsTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setupSessionsObserver();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter < CharSequence > rangeTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_range_type, android.R.layout.simple_spinner_item);
        rangeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRangeTypeSpinner.setAdapter(rangeTypeAdapter);
        mRangeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mXAxisFormatter.setRangeType(SpinnerRangeType.values()[i]);
                setupSessionsObserver();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSessionsObserver() {
        LiveData<List<Session>> sessions =
                AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessionsByEndTime();

        sessions.observe(this, sessions1 -> {

            final boolean isDurationType = mStatsTypeSpinner.getSelectedItemPosition() == DURATION.ordinal();

            final LocalDate today          = new LocalDate();
            final LocalDate thisWeekStart  = today.dayOfWeek().withMinimumValue().minusDays(1);
            final LocalDate thisWeekEnd    = today.dayOfWeek().withMaximumValue().plusDays(1);
            final LocalDate thisMonthStart = today.dayOfMonth().withMinimumValue().minusDays(1);
            final LocalDate thisMonthEnd   = today.dayOfMonth().withMaximumValue().plusDays(1);;

            long statsToday = 0;
            long statsThisWeek = 0;
            long statsThisMonth = 0;
            long statsTotal = 0;

            for (Session s : sessions1) {
                final long increment = isDurationType ? s.totalTime : 1;

                final LocalDate crt = new LocalDate(new Date(s.endTime));
                if (crt.isEqual(today)) {
                    statsToday += increment;
                }
                if (crt.isAfter(thisWeekStart) && crt.isBefore(thisWeekEnd)) {
                    statsThisWeek += increment;
                }
                if (crt.isAfter(thisMonthStart) && crt.isBefore(thisMonthEnd)) {
                    statsThisMonth += increment;
                }
                if (isDurationType) {
                    statsTotal += increment;
                }
            }
            if (!isDurationType) {
                statsTotal = sessions1.size();
            }

            mStatsToday.setText(isDurationType || statsToday == 0
                    ? formatMinutes(statsToday)
                    : Long.toString(statsToday));
            mStatsThisWeek.setText(isDurationType || statsToday == 0
                    ? formatMinutes(statsThisWeek)
                    : Long.toString(statsThisWeek));
            mStatsThisMonth.setText(isDurationType || statsToday == 0
                    ? formatMinutes(statsThisMonth)
                    : Long.toString(statsThisMonth));
            mStatsTotal.setText(isDurationType || statsToday == 0 ?
                    formatMinutes(statsTotal)
                    : Long.toString(statsTotal));

            final LineData data = generateChartData(sessions1);

            mChart.moveViewToX(data.getXMax());
            mChart.setData(data);
            mChart.getData().setHighlightEnabled(false);

            mChart.getAxisLeft().setAxisMinimum(0f);
            mChart.getAxisLeft().setAxisMaximum(isDurationType ? 60f : 6f);

            final int visibleXRange = pxToDp(mChart.getWidth()) / 46;
            mChart.setVisibleXRangeMaximum(visibleXRange);
            mChart.setVisibleXRangeMinimum(visibleXRange);
            mChart.getXAxis().setLabelCount(visibleXRange);

            if (sessions1.size() > 0 && data.getYMax() >= (isDurationType ? 60 : 6f)) {
                mChart.getAxisLeft().resetAxisMaximum();
            }

            mChart.notifyDataSetChanged();
        });
    }

    private void setupChart() {
        mChart.setXAxisRenderer(new CustomXAxisRenderer(
                mChart.getViewPortHandler(),
                mChart.getXAxis(),
                mChart.getTransformer(YAxis.AxisDependency.LEFT)));

        mChart.setRendererLeftYAxis(new CustomYAxisRenderer(
                mChart.getViewPortHandler(),
                mChart.getAxisLeft(),
                mChart.getTransformer(YAxis.AxisDependency.LEFT)
                ));

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setTextColor(getActivity().getResources().getColor(R.color.white));
        yAxis.setGranularity(1);
        yAxis.setTextSize(CHART_TEXT_SIZE);
        yAxis.setDrawAxisLine(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setGranularityEnabled(true);
        xAxis.setTextColor(getActivity().getResources().getColor(R.color.white));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        final SpinnerRangeType rangeType =
                SpinnerRangeType.values()[mRangeTypeSpinner.getSelectedItemPosition()];

        mXAxisFormatter = new CustomXAxisFormatter(xValues, rangeType);
        xAxis.setValueFormatter(mXAxisFormatter);
        xAxis.setAvoidFirstLastClipping(false);
        xAxis.setTextSize(CHART_TEXT_SIZE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        mChart.setExtraTopOffset(30f);
        mChart.setExtraBottomOffset(20f);
        mChart.setExtraLeftOffset(-30);
        mChart.getAxisRight().setEnabled(false);
        mChart.getDescription().setEnabled(false);
        mChart.setNoDataText("");
        mChart.setHardwareAccelerationEnabled(true);
        mChart.animateY(500, Easing.EasingOption.EaseOutCubic);
        mChart.getLegend().setEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setScaleEnabled(true);
        mChart.setDragEnabled(true);
        mChart.invalidate();
        mChart.notifyDataSetChanged();
    }

    private LineData generateChartData(List<Session> sessions) {

        final SpinnerStatsType statsType =
                SpinnerStatsType.values()[mStatsTypeSpinner.getSelectedItemPosition()];
        final SpinnerRangeType rangeType =
                SpinnerRangeType.values()[mRangeTypeSpinner.getSelectedItemPosition()];

        final int DUMMY_INTERVAL_RANGE = 15;

        List<Entry> yVals = new ArrayList<>();
        TreeMap<LocalDate, Long> tree = new TreeMap<>();

        // generate dummy data
        LocalDate dummyEnd = new LocalDate().plusDays(1);
        switch (rangeType) {
            case DAYS:
                LocalDate dummyBegin = dummyEnd.minusDays(DUMMY_INTERVAL_RANGE);
                for (LocalDate i = dummyBegin; i.isBefore(dummyEnd); i = i.plusDays(1)) {
                    tree.put(i, 0L);
                }
                break;
            case WEEKS:
                dummyBegin = dummyEnd.minusWeeks(DUMMY_INTERVAL_RANGE).dayOfWeek().withMinimumValue();
                for (LocalDate i = dummyBegin; i.isBefore(dummyEnd); i = i.plusWeeks(1)) {
                    tree.put(i, 0L);
                }
                break;
            case MONTHS:
                dummyBegin = dummyEnd.minusMonths(DUMMY_INTERVAL_RANGE);
                for (LocalDate i = dummyBegin; i.isBefore(dummyEnd); i = i.plusMonths(1)) {
                    tree.put(i, 0L);
                }
                break;
        }

        // this is to sum up entries from the same day for visualization
        for (int i = 0; i < sessions.size(); ++i) {
            LocalDate localTime = new LocalDate();
            switch (rangeType) {
                case DAYS:
                    localTime = new LocalDate(new Date(sessions.get(i).endTime));
                    break;
                case WEEKS:
                    localTime = new LocalDate(new Date(sessions.get(i).endTime)).dayOfWeek().withMinimumValue();
                    break;
                case MONTHS:
                    localTime = new LocalDate(new Date(sessions.get(i).endTime)).dayOfMonth().withMinimumValue();
                    break;
            }

            if (!tree.containsKey(localTime)) {
                tree.put(localTime, statsType == DURATION ? sessions.get(i).totalTime : 1);
            } else {
                tree.put(localTime, tree.get(localTime)
                        + (statsType == DURATION ? sessions.get(i).totalTime : 1));
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
        return new LineData(generateLineDataSet(yVals, ContextCompat.getColor(getContext(), R.color.cyan)));
    }

    private LineDataSet generateLineDataSet(List<Entry> entries, int color) {
        LineDataSet set = new LineDataSet(entries, "");
        set.setColor(color);
        set.setLineWidth(3f);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.disableDashedLine();
        set.setDrawFilled(true);
        set.setDrawValues(false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            set.setDrawFilled(false);
            set.setLineWidth(2f);
            set.setCircleSize(4f);
            set.setDrawCircleHole(true);
        }
        return set;
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
