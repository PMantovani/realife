package com.example.mantovani.makeitdark;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.mantovani.makeitdark.data.ProductivityContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
    long hourOn;
    long hourOff;
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

        // This will set an alarm every hour to get hourly info about productivity
        setAlarm();

        super.onCreate();
    }

    public void setAlarm() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive( Context context, Intent _ ) {
                Log.d("MAKEITDARK", "SCHEDULER: "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = pm.isScreenOn();
                // Add elapsed time since last lock/unlock
                if (isScreenOn)
                    hourOn += (System.currentTimeMillis() - lastScreenUnlock);
                else
                    hourOff += (System.currentTimeMillis() - lastScreenLock);

                float percentage = (float)(hourOn)/(float)(hourOff+hourOn);

                // Get past hour, so if your alarm received at 15:01, you will update values
                // from 14:00 to 15:00.
                Calendar lastHour = new GregorianCalendar();
                lastHour.add(GregorianCalendar.HOUR_OF_DAY, -1);
                int hour = lastHour.get(GregorianCalendar.HOUR_OF_DAY);
                Date dateLastHour = lastHour.getTime();

                // Get format of hour
                String date = new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour);
                Uri dateUri = ProductivityContract.DayEntry.buildDay(date);
                Cursor cursor = getContentResolver().query(dateUri, null, null, null, null);
                // Date already exists in db, update
                if (cursor.moveToFirst()) {
                    Uri updateUri = ProductivityContract.DayEntry.CONTENT_URI;
                    ContentValues values = new ContentValues();
                    // Integer.toString() should be the String equivalent
                    // of the hour column in the contract
                    values.put("h"+Integer.toString(hour), percentage);
                    String[] selections = {new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour)};
                    getContentResolver().update(updateUri, values, "DATE = ?", selections);
                }
                // Create new record for date in db, insert
                else {
                    Uri insertUri = ProductivityContract.DayEntry.CONTENT_URI;
                    ContentValues values = new ContentValues();
                    values.put("h"+Integer.toString(hour), percentage);
                    values.put(ProductivityContract.DayEntry.COLUMN_DATE,
                            new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour));
                    getContentResolver().insert(insertUri, values);
                }
                Log.d("MAKEITDARK", "INSERTED TO DB LAST HOUR: "+Float.toString(percentage));
                cursor.close();

                hourOff = 0;
                hourOn = 1;
                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_hour_off_key), hourOff)
                        .putLong(getString(R.string.pref_hour_on_key), hourOn)
                        .apply();
                //context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
            }
        };

        this.registerReceiver( receiver,
                new IntentFilter("com.example.mantovani.makeitdark.hourlyreceiver") );

        Calendar calendar = new GregorianCalendar();
        // Next schedule is for the next hour rounded up
        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long timeInMillis = calendar.getTimeInMillis();

        PendingIntent pintent = PendingIntent.getBroadcast( this, 0,
                new Intent("com.example.mantovani.makeitdark.hourlyreceiver"), 0 );
        AlarmManager alarm = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
                60 * 60 * 1000, pintent);
    }

    private void readData() {
        // Get shared preferences for data like total time on and off
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Standard is 1 so we don't have divisions per zero
        totalTimeOn = sharedPrefs.getLong(getString(R.string.pref_time_on_key), 1);
        totalTimeOff = sharedPrefs.getLong(getString(R.string.pref_time_off_key), 0);
        hourOn = sharedPrefs.getLong(getString(R.string.pref_hour_on_key), 1);
        hourOff = sharedPrefs.getLong(getString(R.string.pref_hour_off_key), 0);
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
                    hourOff += diff;
                }
                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_time_off_key), totalTimeOff)
                        .putLong(getString(R.string.pref_hour_off_key), hourOff)
                        .putLong(getString(R.string.pref_last_unlock_key), lastScreenUnlock)
                        .apply();

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
                    hourOn += diff;
                }
                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_time_on_key), totalTimeOn)
                        .putLong(getString(R.string.pref_hour_on_key), hourOn)
                        .putLong(getString(R.string.pref_last_lock_key), lastScreenLock)
                        .apply();
                Log.d("MAKEITDARK", "RECEIVED ACTION_SCREEN_OFF");
                // Save totalTimeOn to database
                //context.getContentResolver(); maybe?
            }
            else {
                throw new UnsupportedOperationException("Unknown broadcast received.");
            }
        }
    }
}
