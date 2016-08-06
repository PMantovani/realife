package com.example.mantovani.makeitdark;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mantovani.makeitdark.data.ProductivityContract;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Tab1Fragment extends Fragment {

    View rootView;
    BarChart barChart;
    BarDataSet barDataSet;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_1, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        drawBarChart();

        super.onResume();
    }

    private void drawBarChart() {
        barChart = (BarChart) getActivity().findViewById(R.id.bar_graph);

        List<BarEntry> entries = new ArrayList<>();
        int colors[] = new int[24];
        ContentResolver resolver = getActivity().getContentResolver();

        // Get format of hour
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Uri dateUri = ProductivityContract.DayEntry.buildDay(date);
        // Gets current hour of the day
        Calendar cal = new GregorianCalendar();
        int hourNow = cal.get(Calendar.HOUR_OF_DAY);

        // Query database for day usage information
        Cursor cursor = resolver.query(dateUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i=0; i<24; i++) {
                int minutes = 0;
                if (i != hourNow) { // Just get the info from db
                    minutes = (int)(cursor.getFloat(cursor.getColumnIndex("h" + Integer.toString(i)))/(60*1000));
                    entries.add(new BarEntry(i, minutes));
                }
                else { // Current hour = calculate
                    // Implements percentage for the current hour, since it's still not in the database
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    float hourOn = (float) sharedPrefs.getLong(getString(R.string.pref_hour_on), 1);

                    hourOn = Utilities.addTimeDiffFromLastUnlock(getContext(), (long)hourOn);

                    minutes = (int)hourOn/(60*1000);
                    entries.add(new BarEntry(hourNow, minutes));
                }
                colors[i] = Utilities.calculateColor(minutes); // Calculate the color of the entry
            }
        }
        else {
            // In case no data is available, display TextView with the info
            //TextView noData = (TextView) getActivity().findViewById(R.id.no_data_textView);
            //noData.setVisibility(TextView.VISIBLE);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            float hourOn = (float) sharedPrefs.getLong(getString(R.string.pref_hour_on), 1);

            hourOn = Utilities.addTimeDiffFromLastUnlock(getContext(), (long)hourOn);

            int minutes = (int)hourOn/(60*1000);
            entries.add(new BarEntry(hourNow, minutes));
        }
        cursor.close();

        barDataSet = new BarDataSet(entries, "% on phone");
        barDataSet.setValueFormatter(new MyValueFormatter());
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barDataSet.setValueTextSize(8f);
        barDataSet.setColors(colors); // Insert colors in graph

        final String[] xValues = new String[] {"12AM", "1AM", "2AM", "3AM", "4AM", "5AM"
                , "6AM", "7AM", "8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM",
                "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM"};

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xValues[(int) value % xValues.length];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setLabelRotationAngle(90f);
        //xAxis.setLabelCount(24, false);
        //xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = barChart.getAxisLeft();
        //leftAxis.setDrawLabels(false); // no axis labels
        leftAxis.setDrawAxisLine(false); // no axis line
        //leftAxis.setDrawGridLines(false); // no grid lines
        leftAxis.setDrawZeroLine(true); // draw a zero line
        leftAxis.setAxisMaxValue(60f); // set maximum value to 100%
        barChart.getAxisRight().setEnabled(false); // no right axis


        BarData data = new BarData(barDataSet);
        barChart.setData(data);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setGridBackgroundColor(0);
        barChart.animateXY(500,500);
        barChart.setDescription("");
        barChart.getLegend().setEnabled(false);

        barChart.invalidate();


    }

    // Formatter class to remove decimal digits on bar graph
    class MyValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use zero decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            if (value >= 1) // If more than one minute
                // Multiplies by 60*1000 because formatTime works in milliseconds
                return Utilities.formatTime((long) value*60*1000, false, getContext());
                //return mFormat.format(value) + "m";//"%"; // e.g. append a % sign
            else
                return "";
        }
    }
}