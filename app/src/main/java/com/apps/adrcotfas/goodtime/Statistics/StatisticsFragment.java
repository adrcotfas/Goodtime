package com.apps.adrcotfas.goodtime.Statistics;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Database.Session;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.databinding.StatisticsMainBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class StatisticsFragment extends Fragment {

    private LineChart mChart;
    private Spinner mSpinner;
    private TextView mTotal;
    private Button mAddEntryButton;

    private List<String> xValues = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StatisticsMainBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.statistics_main, container, false);
        View view = binding.getRoot();

        mChart = binding.chart;
        mSpinner = binding.spinner;
        mTotal = binding.total;
        mAddEntryButton = binding.addEntryButton;
        mAddEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEntryDialog();
            }
        });

        LiveData<List<Session>> sessions =
                AppDatabase.getDatabase(getActivity().getApplicationContext()).sessionModel().getAllSessions();

        sessions.observe(this, new Observer<List<Session>>() {
            @Override
            public void onChanged(List<Session> sessions) {
                long minutes = 0;
                for (Session s : sessions) {
                    minutes += s.totalTime;
                }
                mTotal.setText("Total work duration: " + minutes + " minutes");

                LineData data = generateChartData(sessions);
                if (data.getEntryCount() != 0) {
                    mChart.setData(data);
                    XAxis xAxis = mChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    mChart.getData().setHighlightEnabled(false);
                    mChart.getXAxis().setTextColor(getActivity().getResources().getColor(R.color.white));
                    mChart.getAxisRight().setEnabled(false);
                    mChart.getDescription().setEnabled(false);
                    mChart.setPinchZoom(false);
                    mChart.getLegend().setEnabled(false);
                    mChart.getAxisLeft().setTextColor(getActivity().getResources().getColor(R.color.white));
                    mChart.getAxisLeft().setGranularity(1);
                    mChart.setPinchZoom(false);
                    mChart.setScaleEnabled(true);
                    mChart.setDragEnabled(true);

                    mChart.setVisibleXRangeMaximum(4);

                    mChart.getXAxis().setGridColor(getActivity().getResources().getColor(R.color.transparent));
                    mChart.getXAxis().setGranularityEnabled(true);
                    IAxisValueFormatter formatter = new IAxisValueFormatter() {

                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {
                            // Dirty fix for a library bug. I have to report it online because 'value' returns old values even if the dataset is changed
                            if (value < xValues.size() && value > 0) {
                                return xValues.get((int) value);
                            } else {
                                return "";
                            }
                        }
                    };
                    xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
                    xAxis.setValueFormatter(formatter);
                    xAxis.setAvoidFirstLastClipping(true);
                } else {
                    mChart.setData(null);
                }
            }
        });

        return view;
    }

    private LineData generateChartData(List<Session> sessions) {
        List<String> xVals = new ArrayList<>();
        List<Entry> yVals = new ArrayList<>();

        Map<LocalDate, Long> dateAndDurations = new HashMap<>();

        TreeMap<LocalDate, Long> sorted = new TreeMap<>();

        for (int i = 0; i < sessions.size(); ++i) {
            LocalDate localTime = new LocalDate(new Date(sessions.get(i).endTime));

            if (!dateAndDurations.containsKey(localTime)) {
                dateAndDurations.put(localTime, sessions.get(i).totalTime);
            } else {
                dateAndDurations.put(localTime, dateAndDurations.get(localTime) + sessions.get(i).totalTime);
            }
        }
        //TODO: find more elegant solution to sort
        sorted.putAll(dateAndDurations);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yy");

        int i = 0;
        for (LocalDate time : sorted.keySet()) {
            yVals.add(new Entry(i, sorted.get(time)));
            xVals.add(time.toString(fmt));
            ++i;
        }

        xValues = xVals;
        return new LineData(generateLineDataSet(yVals, ContextCompat.getColor(getContext(), R.color.cyan)));
    }

    private LineDataSet generateLineDataSet(List<Entry> vals, int color) {
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals, "");

        set1.setColor(color);
        set1.setLineWidth(3f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.disableDashedLine();
        set1.setDrawFilled(true);
        set1.setDrawValues(false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            set1.setDrawFilled(false);
            set1.setLineWidth(2f);
            set1.setCircleSize(4f);
            set1.setDrawCircleHole(true);
        }
        return set1;
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
