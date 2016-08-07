package com.example.mantovani.makeitdark;

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
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mantovani on 12-Jul-16.
 */
public class Tab0Fragment extends Fragment {
    PieChart mPieChart;
    View rootView;
    TextView dailyOnTextView;
    SeekBar goalSeekBar;
    SharedPreferences sharedPrefs;
    Timer guiUpdateTimer;
    long dayOn;
    long goal;

    final Handler myHandler = new Handler();
    TextRunnable myRunnable;

    public class TextRunnable implements Runnable {
        long counter = dayOn;
        @Override
        public void run() {
            // Sets text view and increases one second in total time on
            counter += 1000;
            dailyOnTextView.setText(Utilities.formatTime(counter, getString(R.string.millisec_letter), true, getContext()));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get our shared preferences
        sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        // Loads total time device is on
        dayOn = sharedPrefs.getLong(getString(R.string.pref_day_on), 0);
        goal = sharedPrefs.getLong(getString(R.string.pref_goal_key), 60); // Default goal = 60 min

        dayOn += Utilities.timeDiffFromLastUnlock(getContext());
        dayOn += sharedPrefs.getLong(getString(R.string.pref_hour_on), 0); // Add current hour On

        // Initialize runnable for text update
        myRunnable = new TextRunnable();

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_0, container, false);
        dailyOnTextView = (TextView) rootView.findViewById(R.id.time_on_textView);

        goalSeekBar = (SeekBar) rootView.findViewById(R.id.goal_seekBar);
        goalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView goalTextView = (TextView) rootView.findViewById(R.id.goal_textView);
                goal = progress;
                if (goal != 241)
                    goalTextView.setText(Utilities.formatTime(goal, getString(R.string.minutes_letter), false, getContext()));
                else {
                    goalTextView.setText(getString(R.string.no_goal));
                    goal = -999;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drawPieChart(); // Updates chart

                // Saves goal for when leaving the fragment
                sharedPrefs.edit().putLong(getString(R.string.pref_goal_key), goal).apply();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        // Draws the chart
        drawPieChart();

        // Loads number of picks today
        TextView picksTextView = (TextView) rootView.findViewById(R.id.picks_textView);
        picksTextView.setText(""+sharedPrefs.getLong(getString(R.string.pref_picks_today), 0));

        // Loads goal value into seek bar
        TextView goalTextView = (TextView) getActivity().findViewById(R.id.goal_textView);
        goalTextView.setText(Utilities.formatTime(goal, getString(R.string.minutes_letter), false, getContext()));
        SeekBar goalSeekBar = (SeekBar) getActivity().findViewById(R.id.goal_seekBar);
        goalSeekBar.setProgress((int) goal);

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

        // Entries in PieChart: will have total time on and total time off
        List<PieEntry> entries = new ArrayList<>();

        final long minutesInADay = 24 * 60;
        long dailyInMinutes = dayOn /(1000 * 60);
        int[] colors;
        if (dailyInMinutes > goal) {
            // User has exceeded goal, put only two entries in graph
            entries.add(new PieEntry(minutesInADay-dailyInMinutes, getString(R.string.off)));
            entries.add(new PieEntry(dailyInMinutes, getString(R.string.on)));
            // Blue and red
            colors = new int[] {Color.rgb(153,204,255), Color.rgb(255,153,153)};
        }
        else {
            // Still some usage left to goal
            entries.add(new PieEntry(minutesInADay-goal, getString(R.string.off)));
            entries.add(new PieEntry(dailyInMinutes, getString(R.string.on)));
            entries.add(new PieEntry(goal-dailyInMinutes, getString(R.string.toGoal)));
            // Blue, red and gray
            colors = new int[] {Color.rgb(153,204,255), Color.rgb(255,153,153),
                    Color.rgb(140, 140, 140)};
        }

        PieDataSet dataSet = new PieDataSet(entries, "% on phone");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(16f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Utilities.formatTime((long)value, getString(R.string.minutes_letter), false, getActivity());
            }
        });

        PieData data = new PieData(dataSet);
        mPieChart.setData(data);
        mPieChart.setEntryLabelColor(Color.BLACK);
        //mPieChart.setEntryLabelTextSize(20f);
        mPieChart.animateXY(500, 500);
        mPieChart.setCenterText(getString(R.string.screen_usage));
        mPieChart.setCenterTextSize(15f);
        mPieChart.setDescription("");
        mPieChart.getLegend().setEnabled(false);

        mPieChart.invalidate();
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
                    .putLong(getString(R.string.pref_last_unlock), System.currentTimeMillis())
                    .putLong(getString(R.string.pref_last_lock), System.currentTimeMillis())
                    .putLong(getString(R.string.pref_day_on), 0)
                    .apply();
        }
        return super.onOptionsItemSelected(item);
    }
}