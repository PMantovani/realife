package com.example.mantovani.makeitdark;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mantovani on 30-Jun-16.
 */
public class MonitorService extends Service {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    long totalTimeOn;
    long totalTimeOff;
    long lastScreenLock;
    long lastScreenUnlock;
    IntentFilter filter;
    ScreenBroadReceiver receiver;
    SharedPreferences sharedPrefs;

    @Override
    public void onCreate() {
        // Initialize counters
        readData();

        filter = new IntentFilter();
        receiver = new ScreenBroadReceiver();

        // Schedule a timer for updating the database every one hour
        Timer scheduler = new Timer();
        Calendar calendar = new GregorianCalendar();
        // Next schedule is for the next hour rounded up
        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long timeInMillis = calendar.getTimeInMillis();
        Date date = new Date(timeInMillis);
        scheduler.scheduleAtFixedRate(new HourlyTimer(), date, 3600000);

        super.onCreate();
    }

    private void readData() {
        // Get shared preferences for data like total time on and off
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        totalTimeOn = sharedPrefs.getLong(getString(R.string.pref_time_on_key), 1);
        totalTimeOff = sharedPrefs.getLong(getString(R.string.pref_time_off_key), 0);
        lastScreenLock = sharedPrefs.getLong(getString(R.string.pref_last_lock_key),
                System.currentTimeMillis());
        lastScreenUnlock = sharedPrefs.getLong(getString(R.string.pref_last_unlock_key),
                System.currentTimeMillis());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Start broadcast receiver */
        /* Has to pass actions */
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SHUTDOWN);

        registerReceiver(receiver, filter);

        return START_STICKY;
    }

    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class HourlyTimer extends TimerTask {
        @Override
        public void run() {
            postToastMessage("SCHEDULER DEU BOA");
            Log.d("MAKEITDARK", "SCHEDULER DEU BOA");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // Unregister BroadcastReceiver on destroy of service
        unregisterReceiver(receiver);

        super.onDestroy();
    }

    public class ScreenBroadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            readData();

            // User turned screen on
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                //readData();
                lastScreenUnlock = System.currentTimeMillis();
                long diff = lastScreenUnlock - lastScreenLock;
                if (diff > 0) {
                    totalTimeOff += diff;
                }
                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_time_off_key), totalTimeOff)
                        .putLong(getString(R.string.pref_last_unlock_key), lastScreenUnlock)
                        .commit();

                Log.d("MAKEITDARK", "RECEIVED ACTION_SCREEN_ON");
                // Save totalTimeOff to database
                //context.getContentResolver(); maybe?
            }
            // User turned screen off
            else if (action.equals(Intent.ACTION_SCREEN_OFF)
                    || action.equals(Intent.ACTION_SHUTDOWN)) {
                //readData();
                lastScreenLock = System.currentTimeMillis();
                long diff = lastScreenLock - lastScreenUnlock;
                if (diff > 0) {
                    totalTimeOn += diff;
                }
                else
                    Log.d("MAKEITDARK", "DIFFERENCE OF TIME WAS NEGATIVE!");
                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_time_on_key), totalTimeOn)
                        .putLong(getString(R.string.pref_last_lock_key), lastScreenLock)
                        .commit();
                Log.d("MAKEITDARK", "RECEIVED ACTION_SCREEN_OFF");
                // Save totalTimeOn to database
                //context.getContentResolver(); maybe?
            }
            else {
                //   throw new Throwable("Receiver got unknown action");
            }
        }
    }
}
