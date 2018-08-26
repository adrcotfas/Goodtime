package com.apps.adrcotfas.goodtime.Statistics;

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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class StatisticsFragment extends Fragment {

    private LineChart mChart;
    private TextView mTotal;
    private Button mAddEntryButton;
    private Button mDeleteEntriesButton;
    private TextView mSticky;
    private LinearLayout mLayout;

    private List<LocalDate> xValues = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StatisticsMainBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.statistics_main, container, false);
        View view = binding.getRoot();

        mChart = binding.chart;
        mTotal = binding.total;
        mAddEntryButton = binding.addEntryButton;
        mAddEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEntryDialog();
            }
        });
        mSticky = binding.sticky;
        mLayout = binding.layout;

        mChart.notifyDataSetChanged();

        final float textSize = 12f;
        mSticky.setTextSize(COMPLEX_UNIT_DIP, textSize);
        mChart.getXAxis().setTextSize(textSize);
        mChart.getAxisLeft().setTextSize(textSize);

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
                float ty = mChart.getY() - rho*yo - 10f - 0.85f*textSize*rho;
                float tx = mChart.getX() + rho*xo;

                mSticky.setTranslationY(ty);
                mSticky.setTranslationX(tx);
            }
        });

        mSticky.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);

        mChart.setExtraLeftOffset(10f);
        mChart.setExtraBottomOffset(10f);

        mDeleteEntriesButton = binding.deleteEntriesButton;
        mDeleteEntriesButton.setOnClickListener(new View.OnClickListener() {
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

                YAxis yAxis = mChart.getAxisLeft();
                yAxis.setAxisMaximum(100f);
                yAxis.setAxisMinimum(-5f);

                mChart.notifyDataSetChanged();
                LineData data = generateChartData(sessions);
                if (data.getEntryCount() != 0) {
                    mChart.setData(data);
                    XAxis xAxis = mChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    xAxis.setValueFormatter(new StickyDateAxisValueFormatter(xValues, mChart, mSticky));
                    xAxis.setAvoidFirstLastClipping(true);

                    mChart.setVisibleXRangeMaximum(8);

                    mChart.moveViewToX(data.getXMax());

                } else {
                    mChart.setData(new LineData());
                }
                mChart.getData().setHighlightEnabled(false);
                mChart.getXAxis().setTextColor(getActivity().getResources().getColor(R.color.white));
                mChart.getAxisRight().setEnabled(false);
                mChart.getDescription().setEnabled(false);
                mChart.setHardwareAccelerationEnabled(true);
                mChart.animateY(1000, Easing.EasingOption.EaseOutCubic);
                mChart.getLegend().setEnabled(false);
                mChart.getAxisLeft().setTextColor(getActivity().getResources().getColor(R.color.white));
                mChart.getAxisLeft().setGranularity(1);
                mChart.setPinchZoom(false);
                mChart.setScaleEnabled(true);
                mChart.setDragEnabled(true);
                mChart.getXAxis().setGridColor(getActivity().getResources().getColor(R.color.transparent));
                mChart.getXAxis().setGranularityEnabled(true);
                mChart.invalidate();
                mChart.notifyDataSetChanged();

            }
        });

        return view;
    }

    private LineData generateChartData(List<Session> sessions) {
        List<Entry> yVals = new ArrayList<>();

        TreeMap<LocalDate, Long> sorted = new TreeMap<>();

        // this is to sum up entries from the same day for visualization
        for (int i = 0; i < sessions.size(); ++i) {
            LocalDate localTime = new LocalDate(new Date(sessions.get(i).endTime));

            if (!sorted.containsKey(localTime)) {
                sorted.put(localTime, sessions.get(i).totalTime);
            } else {
                sorted.put(localTime, sorted.get(localTime) + sessions.get(i).totalTime);
            }
        }

        if (sorted.size() > 0) {

            xValues.clear();
            int i = 0;
            LocalDate previousTime = sorted.firstKey();

            for (LocalDate time : sorted.keySet()) {
                // visualize intermediate days in case of more than one day between sessions
                while(previousTime.isBefore(time.minusDays(1))) {
                    yVals.add(new Entry(i, 0));
                    previousTime = previousTime.plusDays(1);
                    xValues.add(previousTime);
                    ++i;
                }
                yVals.add(new Entry(i, sorted.get(time)));
                xValues.add(time);
                ++i;
                previousTime = time;
            }
        }

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
