package com.example.mantovani.makeitdark;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mantovani on 12-Jul-16.
 */
public class Tab0Fragment extends Fragment {
    PieChart mPieChart;
    View rootView;
    TextView timeOnTextView;
    SharedPreferences sharedPrefs;
    Timer guiUpdateTimer;
    long timeOn;

    final Handler myHandler = new Handler();
    final Runnable myRunnable = new Runnable() {
        public void run() {
            // Sets text view and increases one second in total time on
            timeOn += 1000;
            timeOnTextView.setText(formatTime(timeOn));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get our shared preferences
        sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        // Loads total time device is on
        timeOn = sharedPrefs.getLong(getString(R.string.pref_time_on_key), 1);

        long now = System.currentTimeMillis();
        long lastUnlock = sharedPrefs.getLong(getString(R.string.pref_last_unlock_key), now);
        long lastLock = sharedPrefs.getLong(getString(R.string.pref_last_lock_key), now);
        long diff = now - lastUnlock;
        if (lastUnlock >= lastLock) {
            timeOn += diff;
        }

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_0, container, false);
        timeOnTextView = (TextView) rootView.findViewById(R.id.time_on_textView);

        return rootView;
    }

    @Override
    public void onResume() {
        //setTextViews();

        drawPieChart();

        // Start a scheduled timer to update the GUI every second
        guiUpdateTimer = new Timer();
        guiUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { updateGUI(); }
        }, 0, 1000);

        super.onResume();
    }

    @Override
    public void onPause() {
        guiUpdateTimer.cancel();
        guiUpdateTimer.purge();
        super.onPause();
    }

    private void updateGUI() {
        myHandler.post(myRunnable);
    }

    private void drawPieChart() {
        // Get reference to PieChart in layout xml
        mPieChart = (PieChart) getActivity().findViewById(R.id.pie_chart);

        // Get content resolver to query database
        ContentResolver resolver = getActivity().getContentResolver();

        // Entries in PieChart: will have total time on and total time off
        List<PieEntry> entries = new ArrayList<>();
        long timeOff = sharedPrefs.getLong(getString(R.string.pref_time_off_key), 0);

        // Set TextView for timeOn
        //TextView timeOnTextView = (TextView) getActivity().findViewById(R.id.time_on_textView);
        //timeOnTextView.setText(formatTime(timeOn));

        entries.add(new PieEntry(timeOn, getString(R.string.on)));
        entries.add(new PieEntry(timeOff, getString(R.string.off)));

        PieDataSet dataSet = new PieDataSet(entries, "% on phone");
        dataSet.setValueFormatter(new PercentFormatter());
        // Red and blue
            int[] colors = {new Color().rgb(255,153,153), new Color().rgb(153,204,255)};
        dataSet.setColors(colors);
        dataSet.setValueTextSize(20f);

        PieData data = new PieData(dataSet);
        mPieChart.setData(data);
        mPieChart.setEntryLabelColor(Color.BLACK);
        mPieChart.setEntryLabelTextSize(20f);
        mPieChart.animateXY(500, 500);
        mPieChart.setUsePercentValues(true);
        mPieChart.setCenterText(getString(R.string.screen_usage));
        mPieChart.setCenterTextSize(15f);
        mPieChart.setDescription("");
        mPieChart.getLegend().setEnabled(false);

        mPieChart.invalidate();

    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds/1000;
        String formatted;

        int days = (int)(seconds/86400);
        int hours = (int)(seconds-(days*86400))/3600;
        int minutes = (int)(seconds-(days*86400)-(hours*3600))/60;
        seconds = (int)(seconds-(days*86400)-(hours*3600)-(minutes*60));

        if (days >= 1) { // More than a day
            formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s%5$d%6$s%7$d%8$d", days,
                    getString(R.string.days_letter), hours, getString(R.string.hours_letter), minutes, getString(R.string.minutes_letter), seconds, getString(R.string.seconds_letter));
        }
        else if (hours >= 1) {  // More than an hour
            formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s%5$d%6$s", hours,
                    getString(R.string.hours_letter), minutes, getString(R.string.minutes_letter), seconds, getString(R.string.seconds_letter));
        }
        else if (minutes >= 1) { // More than a minute
            formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s", minutes,
                    getString(R.string.minutes_letter), seconds, getString(R.string.seconds_letter));
        }
        else {
            formatted = String.format(Locale.US, "%1$d%2$s", seconds,
                    getString(R.string.seconds_letter));
        }
        return formatted;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // Delete all recorded time
        if (id == R.id.delete_all) {
            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getContext());

            sharedPrefs.edit()
                    .clear()
                    .putLong(getString(R.string.pref_last_unlock_key), System.currentTimeMillis())
                    .putLong(getString(R.string.pref_last_lock_key), System.currentTimeMillis())
                    .putLong(getString(R.string.pref_time_on_key), 1)
                    .putLong(getString(R.string.pref_time_off_key), 0)
                    .commit();
          //  setTextViews();
        }
        return super.onOptionsItemSelected(item);
    }
}


/*
    private void setTextViews() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        long timeOn = sharedPrefs.getLong(getString(R.string.pref_time_on_key), 1);
        // Needs to avoid timeOn being 0 because we would divide by 0 crashing the program
        if (timeOn == 0)
            timeOn = 1;

        long now = System.currentTimeMillis();
        long lastUnlock = sharedPrefs.getLong(getString(R.string.pref_last_unlock_key), now);
        long lastLock = sharedPrefs.getLong(getString(R.string.pref_last_lock_key), now);
        long diff = now - lastUnlock;
        if (lastUnlock >= lastLock) {
            timeOn += diff;
        }

        long timeOff = sharedPrefs.getLong(getString(R.string.pref_time_off_key), 0);

        TextView percentage = (TextView) getActivity().findViewById(R.id.percentage_textView);
        percentage.setText(String.format(Locale.US, "%1$d%%", 100*timeOn/(timeOff+timeOn)));

        TextView totalOn = (TextView) getActivity().findViewById(R.id.hours_lost_textView);
        totalOn.setText(formatTime(timeOn));

        TextView totalOff = (TextView) getActivity().findViewById(R.id.timeOff_textView);
        totalOff.setText(formatTime(timeOff));
    }
*/