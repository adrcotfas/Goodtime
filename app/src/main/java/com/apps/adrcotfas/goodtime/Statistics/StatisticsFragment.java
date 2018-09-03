package com.apps.adrcotfas.goodtime.Statistics;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Database.Session;
import com.apps.adrcotfas.goodtime.R;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static com.apps.adrcotfas.goodtime.Statistics.SpinnerStatsType.DURATION;

public class StatisticsFragment extends Fragment {

    private LineChart mChart;

    private TextView mStatsToday;
    private TextView mStatsThisWeek;
    private TextView mStatsThisMonth;
    private TextView mStatsTotal;

    private TextView mSticky;
    private LinearLayout mLayout;
    private Spinner mStatsTypeSpinner;
    private Spinner mRangeTypeSpinner;
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

        mSticky = binding.sticky;
        mLayout = binding.layout;
        mStatsTypeSpinner = binding.statsTypeSpinner;
        mRangeTypeSpinner = binding.rangeTypeSpinner;
        binding.deleteEntriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete all entries")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                AppDatabase.getDatabase(getContext()).sessionModel().deleteAllSessions();
                                            }
                                        });
                                    }
                                }
                        )
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                .show();
            }
        });
        binding.addEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEntryDialog();
            }
        });

        setupSpinners();
        setupStickyText();
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupStickyText() {
        ViewTreeObserver vto = mLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float xo = mChart.getXAxis().getXOffset();
                float yo = mChart.getXAxis().getYOffset();
                final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float rho = displayMetrics.density;

                // magic numbers
                float ty = mChart.getY() - rho*yo - 10f - 0.85f * CHART_TEXT_SIZE * rho;
                float tx = mChart.getX() + rho*xo;

                mSticky.setTranslationY(ty);
                mSticky.setTranslationX(tx);
                mSticky.setTextSize(COMPLEX_UNIT_DIP, CHART_TEXT_SIZE);
                mSticky.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
            }
        });
    }

    private void setupSessionsObserver() {
        LiveData<List<Session>> sessions =
                AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessions();

        sessions.observe(this, new Observer<List<Session>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(List<Session> sessions) {

                final int statsType = mStatsTypeSpinner.getSelectedItemPosition();
                LineData data = generateChartData(sessions, SpinnerStatsType.values()[statsType]);

                long statsToday = 0;
                long statsThisWeek = 0;
                long statsThisMonth = 0;
                long statsTotal = 0;

                final LocalDate today          = new LocalDate();
                final LocalDate thisWeekStart  = today.dayOfWeek().withMinimumValue().minusDays(1);
                final LocalDate thisWeekEnd    = today.dayOfWeek().withMaximumValue().plusDays(1);
                final LocalDate thisMonthStart = today.dayOfMonth().withMinimumValue().minusDays(1);
                final LocalDate thisMonthEnd   = today.dayOfMonth().withMaximumValue().plusDays(1);;
                for (Session s : sessions) {
                    final LocalDate crt = new LocalDate(new Date(s.endTime));
                    if (crt.isEqual(today)) {
                        statsToday += statsType == DURATION.ordinal() ? s.totalTime : 1;
                    }
                    if (crt.isAfter(thisWeekStart) && crt.isBefore(thisWeekEnd)) {
                        statsThisWeek += statsType == DURATION.ordinal() ? s.totalTime : 1;
                    }
                    if (crt.isAfter(thisMonthStart) && crt.isBefore(thisMonthEnd)) {
                        statsThisMonth += statsType == DURATION.ordinal() ? s.totalTime : 1;
                    }
                    if (statsType == DURATION.ordinal()) {
                        statsTotal += s.totalTime;
                    }
                }
                if (statsType == DURATION.ordinal()) {
                    statsTotal = sessions.size();
                }

                mStatsToday.setText(Long.toString(statsToday));
                mStatsThisWeek.setText(Long.toString(statsThisWeek));
                mStatsThisMonth.setText(Long.toString(statsThisMonth));
                mStatsTotal.setText(Long.toString(statsTotal));

                if (data.getEntryCount() != 0) {
                    mChart.moveViewToX(data.getXMax());
                    mChart.setData(data);
                    mChart.getData().setHighlightEnabled(false);
                    mChart.setVisibleXRangeMaximum(8);
                    mChart.getAxisLeft().resetAxisMaximum();
                } else {
                    mChart.setData(new LineData());
                    mChart.invalidate();
                    mChart.notifyDataSetChanged();
                }
            }
        });
    }

    private void setupChart() {
        YAxis yAxis = mChart.getAxisLeft();
        //yAxis.setAxisMinimum(-3f);
        yAxis.setAxisMaximum(100);
        yAxis.setTextColor(getActivity().getResources().getColor(R.color.white));
        yAxis.setGranularity(1);
        yAxis.setTextSize(CHART_TEXT_SIZE);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setGridColor(getActivity().getResources().getColor(R.color.transparent));
        xAxis.setGranularityEnabled(true);
        xAxis.setTextColor(getActivity().getResources().getColor(R.color.white));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new StickyDateAxisValueFormatter(xValues, mChart, mSticky));
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setTextSize(CHART_TEXT_SIZE);

        // TODO: adapt according to screen density and view width
        mChart.setData(new LineData());

        mChart.setExtraLeftOffset(10f);
        mChart.setExtraBottomOffset(10f);
        mChart.getAxisRight().setEnabled(false);
        mChart.getDescription().setEnabled(false);
        mChart.setNoDataText("");
        mChart.setHardwareAccelerationEnabled(true);
        mChart.animateY(500, Easing.EasingOption.EaseOutCubic);
        mChart.getLegend().setEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(true);
        mChart.setDragEnabled(true);
        mChart.invalidate();
        mChart.notifyDataSetChanged();
    }

    private LineData generateChartData(List<Session> sessions, SpinnerStatsType spinnerStatsType) {
        List<Entry> yVals = new ArrayList<>();

        TreeMap<LocalDate, Long> tree = new TreeMap<>();

        // this is to sum up entries from the same day for visualization
        for (int i = 0; i < sessions.size(); ++i) {
            LocalDate localTime = new LocalDate(new Date(sessions.get(i).endTime));

            if (!tree.containsKey(localTime)) {
                tree.put(localTime, spinnerStatsType == DURATION ? sessions.get(i).totalTime : 1);
            } else {
                tree.put(localTime, tree.get(localTime)
                        + (spinnerStatsType == DURATION ? sessions.get(i).totalTime : 1));
            }
        }

        if (tree.size() > 0) {
            xValues.clear();
            int i = 0;
            LocalDate previousTime = tree.firstKey();

            for (LocalDate time : tree.keySet()) {
                // visualize intermediate days in case of more than one day between sessions
                while(previousTime.isBefore(time.minusDays(1))) {
                    yVals.add(new Entry(i, 0));
                    previousTime = previousTime.plusDays(1);
                    xValues.add(previousTime);
                    ++i;
                }
                yVals.add(new Entry(i, tree.get(time)));
                xValues.add(time);
                ++i;
                previousTime = time;
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

    public void showAddEntryDialog() {
        View promptView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_entry, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()).setTitle("Add entry");
        alertDialogBuilder.setView(promptView);

        final EditText durationEditText = promptView.findViewById(R.id.duration);
        final EditText dateEditText = promptView.findViewById(R.id.date);

        final Calendar c = Calendar.getInstance();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                dateEditText.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);
                                c.set(Calendar.YEAR, year);
                                c.set(Calendar.MONTH, monthOfYear);
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
                dpd.show();
            }
        });

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            long duration = Math.min(Long.parseLong(durationEditText.getText().toString()), 120);
                            if (duration > 0) {
                                addEntry(duration, c.getTimeInMillis());
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getActivity(), "Invalid duration.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                )
                .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void addEntry(final long duration, final long timestamp) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Session session = new Session();
                session.endTime = timestamp;
                session.totalTime = duration;
                AppDatabase.getDatabase(getContext()).sessionModel().addSession(session);
            }
        });
    }
}
