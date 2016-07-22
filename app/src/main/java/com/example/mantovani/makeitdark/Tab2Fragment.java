package com.example.mantovani.makeitdark;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.GregorianCalendar;
import java.util.List;

public class Tab2Fragment extends Fragment {
    BarChart weekBarChart;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_2,container,false);
        return rootView;
    }

    @Override
    public void onResume() {
        // Draws the graph and update any textviews
        updateGUI();
        super.onResume();
    }

    private void updateGUI() {
        weekBarChart = (BarChart) rootView.findViewById(R.id.week_bar_graph);

        Calendar cal = new GregorianCalendar();
        int day = cal.get(Calendar.DATE);

        // Query all weeks' percentages of usage of the week
        float[] percents = queryWeekFromDb();
        // Adds all those values in BarEntries
        List<BarEntry> entries = new ArrayList<>();
        int[] colors = new int[percents.length];
        for (int i=0; i<percents.length; i++) {
            entries.add(new BarEntry(i, percents[i]));
            colors[i] = calculateColor(percents[i]); // Calculate the color of the entry
        }

        // Creates DataSet and sets some configs
        BarDataSet barDataSet = new BarDataSet(entries, "Week"); // Associate entries with dataset
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // YAxis on the left
        barDataSet.setValueTextSize(8f); // Sets text size
        barDataSet.setValueFormatter(new MyValueFormatter()); // Formats Y values through MyValueFormatter class
        barDataSet.setColors(colors);

        // Sets x-axis configurations and labels
        XAxis xAxis = weekBarChart.getXAxis();
        xAxis.setLabelCount(7, true);
        // Array of strings with the abbreviations of week days
        final String[] xLabels = {getString(R.string.sun),getString(R.string.mon),
                getString(R.string.tue),getString(R.string.wed), getString(R.string.thu),
                getString(R.string.fri), getString(R.string.sat)};
        // This formats the labels of the x-axis. We want the abbreviation of the days of the week.
        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // Gets day today
                Calendar cal = new GregorianCalendar();
                // Adds current day of the week to adjust the offset. Subtracts 1 because sunday
                // starts at 1, and at 0 in the string array
                return xLabels[(int)((value)+cal.get(Calendar.DAY_OF_WEEK)-1) % xLabels.length];
            }
            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE); // Labels below and inside the graph
        xAxis.setDrawAxisLine(false); // Do not draw border below

        // Sets configs of the YAxis, aka LeftAxis
        YAxis leftAxis = weekBarChart.getAxisLeft();
        //leftAxis.setDrawLabels(false); // no axis labels
        leftAxis.setDrawAxisLine(false); // no axis line
        //leftAxis.setDrawGridLines(false); // no grid lines
        leftAxis.setDrawZeroLine(true); // draw a zero line
        leftAxis.setAxisMaxValue(100f); // set maximum value to 100%
        weekBarChart.getAxisRight().setEnabled(false); // no right axis

        // Associate chart with dataset. BarData only serves as an intermediate.
        BarData data = new BarData(barDataSet);
        weekBarChart.setData(data);
        // Sets configs for chart
        weekBarChart.setTouchEnabled(true); // Enable touch in the graph
        weekBarChart.setDragEnabled(true);  // Enables drag of the graph
        weekBarChart.setScaleEnabled(false); // Disables scaling
        weekBarChart.setDrawGridBackground(false); // Disables background grid
        weekBarChart.setGridBackgroundColor(0); // Sets background color
        weekBarChart.animateXY(500,500); // Inserts animation when opening graph
        weekBarChart.setDescription(""); // Disables description
        weekBarChart.getLegend().setEnabled(false); // Disables legends
        weekBarChart.invalidate(); // Deploy drawing of graph

    }

    private int calculateColor(float percent) {
        int green = Color.rgb(153, 255, 153);
        int yellow = Color.rgb(255, 255, 153);
        int red = Color.rgb(255, 153, 153);
        if (percent < 10) {
            // Interpolate between green and yellow
            return Color.rgb(153+(int)((10.2)*percent),255,153);
        }
        else if (percent < 25) {
            // Interpolate between yellow and red
            return Color.rgb(255,255-(int)(6.8*(percent-10)),153);
        }
        else {
            // Return red
            return (Color.rgb(255,153,153));
        }
    }

    // Formatter class to remove decimal digits on bar graph and append % sign
    class MyValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use zero decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            if (value >= 1)
                return mFormat.format(value) + "%"; // e.g. append a % sign
            else
                return "";
        }
    }

    private float[] queryWeekFromDb() {
        ContentResolver resolver = getActivity().getContentResolver();
        ArrayList<Float> list = new ArrayList<>();

        // Get today in String format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = new GregorianCalendar();
        String date2 = sdf.format(cal.getTime());
        // Get last week
        cal.add(Calendar.DATE, -7);
        String date1 = sdf.format(cal.getTime());

        Uri dateUri = ProductivityContract.DayEntry.buildWithDayInterval(date1, date2);

        Cursor cursor = resolver.query(dateUri, null, null, null, null);
        if (cursor.moveToFirst() && !cursor.isLast()) {
            while (!cursor.isLast()) {
                String dateCursor = cursor.getString(
                        cursor.getColumnIndex(ProductivityContract.DayEntry.COLUMN_DATE_INT));
                if (dateCursor.equals(sdf.format(cal.getTime()))) {
                    list.add(getAverage(cursor));
                    cursor.moveToNext();
                    cal.add(Calendar.DATE, 1);
                }
                else {
                    // In case the day is not found in the database, insert zero
                    list.add((float)0);
                    cal.add(Calendar.DATE, 1);
                }
            }
        }
        else {
            // In case no data is available, display TextView with the info
            TextView noData = (TextView) getActivity().findViewById(R.id.no_data_textView);
            noData.setVisibility(TextView.VISIBLE);
        }
        cursor.close();

        // Converts to primitive type
        float[] retFloat = new float[list.size()];
        int i = 0;
        for (Float f: list) {
            retFloat[i++] = f;
        }
        return retFloat;
    }

    // Gets average percentage of day in the cursor
    private float getAverage(Cursor cursor) {
        float total = 0;
        for (int i=0; i<24; i++) {
            total += cursor.getFloat(cursor.getColumnIndex("h"+i));
        }
        // Divide by 24 hours of the day
        return (100*total/24);
    }
}