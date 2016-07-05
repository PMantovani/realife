package com.example.mantovani.makeitdark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        float timeOn = sharedPrefs.getFloat(getString(R.string.pref_time_on_key), 1);
        float timeOff = sharedPrefs.getFloat(getString(R.string.pref_time_off_key), 0);

        TextView percentage = (TextView) findViewById(R.id.percentage_textView);

        percentage.setText(Float.toString(100*timeOn/(timeOff+timeOn))+"%");

        Intent serviceIntent = new Intent(this, MonitorService.class);
        this.startService(serviceIntent);
    }
}
