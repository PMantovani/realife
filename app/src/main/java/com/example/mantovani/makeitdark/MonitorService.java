package com.example.mantovani.makeitdark;

import android.app.AlarmManager;
import android.app.NotificationManager;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.mantovani.makeitdark.data.ProductivityContract;

import java.text.SimpleDateFormat;
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
    long dailyOn;
    long hourOn;
    long hourOff;
    long lastScreenLock;
    long lastScreenUnlock;
    IntentFilter filter;
    ScreenBroadReceiver receiver;
    SharedPreferences sharedPrefs;
    PendingIntent pintent;
    AlarmManager alarm;


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
/*
    public void setAlarm() {
        Timer timer = new Timer();
        HourlyTimer hourlyTask = new HourlyTimer();
        Calendar cal = new GregorianCalendar();
        //cal.add(Calendar.HOUR_OF_DAY, 1);
        //cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);

        timer.scheduleAtFixedRate(hourlyTask, cal.getTime(),60*60*1000);
    }

    public class HourlyTimer extends TimerTask {
        public void run () {
            Log.d("MAKEITDARK", "SCHEDULER: "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            createNotification();

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            // Add elapsed time since last lock/unlock
            if (isScreenOn)
                hourOn += (System.currentTimeMillis() - lastScreenUnlock);
            else
                hourOff += (System.currentTimeMillis() - lastScreenLock);

            // Update "lastScreenLock and lastScreenUnlock" so you don't add it twice
            // next time you change your device screen status.
            lastScreenLock = System.currentTimeMillis();
            lastScreenUnlock = System.currentTimeMillis();

            addToDatabase(hourOn);

            hourOff = 0;
            hourOn = 1;
            sharedPrefs.edit()
                    .putLong(getString(R.string.pref_hour_off_key), hourOff)
                    .putLong(getString(R.string.pref_hour_on_key), hourOn)
                    .putLong(getString(R.string.pref_last_lock_key), lastScreenLock)
                    .putLong(getString(R.string.pref_last_unlock_key), lastScreenUnlock)
                    .apply();
        }

        private void addToDatabase(float timeOn) {
            // Get past hour, so if your alarm received at 15:01, you will update values
            // from 14:00 to 15:00. Minutes and seconds don't matter here
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
                //Uri updateUri = ProductivityContract.DayEntry.CONTENT_URI;
                ContentValues values = new ContentValues();
                // Integer.toString() should be the String equivalent
                // of the hour column in the contract
                values.put("h"+Integer.toString(hour), timeOn);
                String[] selections = {new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour)};
                getContentResolver().update(dateUri, values, "DATE = ?", selections);
            }
            // Create new record for date in db, insert
            else {
                //Uri insertUri = ProductivityContract.DayEntry.CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put("h"+Integer.toString(hour), timeOn);
                values.put(ProductivityContract.DayEntry.COLUMN_DATE,
                        new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour));
                values.put(ProductivityContract.DayEntry.COLUMN_DATE_INT,
                        Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(dateLastHour)));
                getContentResolver().insert(dateUri, values);
            }
            Log.d("MAKEITDARK", "INSERTED TO DB LAST HOUR: "+Float.toString(timeOn));
            cursor.close();

            // Test notification
            createNotification();
        }
    }
*/
    public void setAlarm() {

        BroadcastReceiver receiverHourly = new BroadcastReceiver() {
            @Override public void onReceive( Context context, Intent _ ) {
                Log.d("MAKEITDARK", "SCHEDULER: "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = pm.isScreenOn();
                // Add elapsed time since last lock/unlock
                if (isScreenOn)
                    hourOn += (System.currentTimeMillis() - lastScreenUnlock);
                else
                    hourOff += (System.currentTimeMillis() - lastScreenLock);

                Calendar now = new GregorianCalendar();
                Calendar oneHourAgo = new GregorianCalendar();
                oneHourAgo.add(Calendar.HOUR_OF_DAY, -1);
                // Checks if date has changed
                if (now.get(Calendar.DATE) != oneHourAgo.get(Calendar.DATE)) {
                    dailyOn = 0; // Reset daily counter if so
                }

                // Update "lastScreenLock and lastScreenUnlock" so you don't add it twice
                // next time you change your device screen status.
                lastScreenLock = System.currentTimeMillis();
                lastScreenUnlock = System.currentTimeMillis();

                addToDatabase(hourOn);

                hourOff = 0;
                hourOn = 1;
                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_hour_off_key), hourOff)
                        .putLong(getString(R.string.pref_hour_on_key), hourOn)
                        .putLong(getString(R.string.pref_daily_on_key), dailyOn)
                        .putLong(getString(R.string.pref_last_lock_key), lastScreenLock)
                        .putLong(getString(R.string.pref_last_unlock_key), lastScreenUnlock)
                        .apply();
                //context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
                // Repeat the alarm
                Calendar calendar = new GregorianCalendar();
                // Next schedule is for the next hour rounded up
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);/*
                calendar.add(Calendar.MINUTE, 1);
                calendar.set(Calendar.SECOND, 0);*/
                long timeInMillis = calendar.getTimeInMillis();
                // setExact was only implemented after KitKat API 19
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    alarm.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pintent);
            }

            private void addToDatabase(float timeOn) {
                // Get past hour, so if your alarm received at 15:01, you will update values
                // from 14:00 to 15:00. Minutes and seconds don't matter here
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
                    //Uri updateUri = ProductivityContract.DayEntry.CONTENT_URI;
                    ContentValues values = new ContentValues();
                    // Integer.toString() should be the String equivalent
                    // of the hour column in the contract
                    values.put("h"+Integer.toString(hour), timeOn);
                    String[] selections = {new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour)};
                    getContentResolver().update(dateUri, values, "DATE = ?", selections);
                }
                // Create new record for date in db, insert
                else {
                    //Uri insertUri = ProductivityContract.DayEntry.CONTENT_URI;
                    ContentValues values = new ContentValues();
                    values.put("h"+Integer.toString(hour), timeOn);
                    values.put(ProductivityContract.DayEntry.COLUMN_DATE,
                            new SimpleDateFormat("dd/MM/yyyy").format(dateLastHour));
                    values.put(ProductivityContract.DayEntry.COLUMN_DATE_INT,
                            Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(dateLastHour)));
                    getContentResolver().insert(dateUri, values);
                }
                Log.d("MAKEITDARK", "INSERTED TO DB LAST HOUR: "+Float.toString(timeOn));
                cursor.close();

                // Test notification
                createNotification();
            }
        };

        this.registerReceiver( receiverHourly,
                new IntentFilter("com.example.mantovani.makeitdark.hourlyreceiver") );

        Calendar calendar = new GregorianCalendar();
        // Next schedule is for the next hour rounded up
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);/*
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);*/
        long timeInMillis = calendar.getTimeInMillis();

        pintent = PendingIntent.getBroadcast( this, 0,
                new Intent("com.example.mantovani.makeitdark.hourlyreceiver"), 0 );
        alarm = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // setExact was only implemented after KitKat API 19, so we have different implementations
        // for different API levels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            alarm.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pintent);
        else {
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis
                    , 60 * 60 * 1000, pintent);
        }
    }

    public void createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(R.drawable.ic_notification);
        mBuilder.setContentTitle("ReaLife");
        mBuilder.setContentText("You spent "+Long.toString(dailyOn/(60*1000))+" minutes on the phone today!");
        mBuilder.setAutoCancel(true); // Remove notification automatically after clicking in it
        // Create explicit intent to be executed when notification is clicked
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent); // Associate notification with intent
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(100, mBuilder.build());
    }

    private void readData() {
        // Get shared preferences for data like total time on and off
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Standard is 1 so we don't have divisions per zero
        totalTimeOn = sharedPrefs.getLong(getString(R.string.pref_time_on_key), 1);
        totalTimeOff = sharedPrefs.getLong(getString(R.string.pref_time_off_key), 0);
        dailyOn = sharedPrefs.getLong(getString(R.string.pref_daily_on_key), 0);
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
        Timer timer;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            readData();

            // User turned screen on
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                lastScreenUnlock = System.currentTimeMillis();
                long diff = lastScreenUnlock - lastScreenLock;
                if (diff > 0) {
                    totalTimeOff += diff;
                    hourOff += diff;
                }

                // Schedules notification for exceeding goal time limit
                timer = new Timer();
                long goal = 60*1000*sharedPrefs.getLong(getString(R.string.pref_goal_key), 60);
                if (goal > dailyOn && goal != (-999*60*1000)) // Only schedule if daily time is still smaller than goal and user has set a goal
                    timer.schedule(new TimerNotification(), goal-dailyOn);

                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_time_off_key), totalTimeOff)
                        .putLong(getString(R.string.pref_hour_off_key), hourOff)
                        .putLong(getString(R.string.pref_last_unlock_key), lastScreenUnlock)
                        .apply();

                Log.d("MAKEITDARK", "RECEIVED ACTION_SCREEN_ON");
            }
            // User turned screen off
            else if (action.equals(Intent.ACTION_SCREEN_OFF)
                    || action.equals(Intent.ACTION_SHUTDOWN)) {
                lastScreenLock = System.currentTimeMillis();
                long diff = lastScreenLock - lastScreenUnlock;
                if (diff > 0) {
                    totalTimeOn += diff;
                    hourOn += diff;
                    dailyOn += diff;
                }

                // Cancel notification for exceeding timer
                if (timer != null) {
                    timer.purge();
                    timer.cancel();
                }

                sharedPrefs.edit()
                        .putLong(getString(R.string.pref_time_on_key), totalTimeOn)
                        .putLong(getString(R.string.pref_hour_on_key), hourOn)
                        .putLong(getString(R.string.pref_daily_on_key), dailyOn)
                        .putLong(getString(R.string.pref_last_lock_key), lastScreenLock)
                        .apply();
                Log.d("MAKEITDARK", "RECEIVED ACTION_SCREEN_OFF");
            }
            else {
                throw new UnsupportedOperationException("Unknown broadcast received.");
            }
        }
    }

    public class TimerNotification extends TimerTask {
        @Override
        public void run() {
            createNotification();
        }
    }
}
