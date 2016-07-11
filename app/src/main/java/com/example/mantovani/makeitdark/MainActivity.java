package com.example.mantovani.makeitdark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, MonitorService.class);
        this.startService(serviceIntent);
    }

    @Override
    protected void onResume(){
        setTextViews();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
                    PreferenceManager.getDefaultSharedPreferences(this);

            sharedPrefs.edit()
                    .clear()
                    .putLong(getString(R.string.pref_last_unlock_key), System.currentTimeMillis())
                    .putLong(getString(R.string.pref_last_lock_key), System.currentTimeMillis())
                    .putLong(getString(R.string.pref_time_on_key), 1)
                    .putLong(getString(R.string.pref_time_off_key), 0)
                    .commit();
            setTextViews();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTextViews() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        long timeOn = sharedPrefs.getLong(getString(R.string.pref_time_on_key), 1);
        // Needs to avoid timeOn being 0 because we would divide by 0 crashing the program
        if (timeOn == 0)
            timeOn = 1;

        long now = System.currentTimeMillis();
        long lastUnlock = sharedPrefs.getLong(getString(R.string.pref_last_unlock_key), now);
        long lastLock = sharedPrefs.getLong(getString(R.string.pref_last_lock_key), now);
        long diff = now - lastUnlock;
        if (lastUnlock > lastLock) {
            timeOn += diff;
        }

        long timeOff = sharedPrefs.getLong(getString(R.string.pref_time_off_key), 0);

        TextView percentage = (TextView) findViewById(R.id.percentage_textView);
        percentage.setText(String.format(Locale.US, "%1$d%%", 100*timeOn/(timeOff+timeOn)));

        TextView totalOn = (TextView) findViewById(R.id.hours_lost_textView);
        totalOn.setText(formatTime(timeOn));

        TextView totalOff = (TextView) findViewById(R.id.timeOff_textView);
        totalOff.setText(formatTime(timeOff));
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds/1000;
        String formatted;

        int days = (int)(seconds/86400);
        int hours = (int)(seconds-(days*86400))/3600;
        int minutes = (int)(seconds-(days*86400)-(hours*3600))/60;
        seconds = (int)(seconds-(days*86400)-(hours*3600)-(minutes*60));

        if (days >= 1) { // More than a day
            formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s", days,
                    getString(R.string.days_letter), hours, getString(R.string.hours_letter));
        }
        else if (hours >= 1) {  // More than an hour
            formatted = String.format(Locale.US, "%1$d%2$s%3$d%4$s", hours,
                    getString(R.string.hours_letter), minutes, getString(R.string.minutes_letter));
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
}
