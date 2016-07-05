package com.example.mantovani.makeitdark;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Mantovani on 30-Jun-16.
 */
public class MonitorService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    long totalTimeOn = 0;
    long totalTimeOff = 0;
    long lastScreenLock;
    long lastScreenUnlock;
    IntentFilter filter;
    ScreenBroadReceiver receiver;
    SharedPreferences sharedPrefs;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MonitorService(String name) {
        super(name);
    }

    public MonitorService() {
        super(null);
    }

    @Override
    public void onCreate() {
        // Initialize counters
        lastScreenLock = System.currentTimeMillis();
        lastScreenUnlock = System.currentTimeMillis();
        filter = new IntentFilter();
        receiver = new ScreenBroadReceiver();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /* Start broadcast receiver */
        /* Has to pass actions */
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(receiver, filter);

        while(true); // Does the service destroys itself after this?
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);   // A forum told this is necessary? Free resources?

        super.onDestroy();
    }

    public class ScreenBroadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                lastScreenUnlock = System.currentTimeMillis();
                totalTimeOff += (lastScreenUnlock - lastScreenLock);
                sharedPrefs.edit()
                        .putFloat(getString(R.string.pref_time_off_key), totalTimeOff)
                        .apply();
                Log.d("MAKEITDARK", "RECEIVED ACTION_SCREEN_ON");
                // Save totalTimeOff to database
                //context.getContentResolver(); maybe?

            }
            else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                lastScreenLock = System.currentTimeMillis();
                totalTimeOn += (lastScreenLock - lastScreenUnlock);
                sharedPrefs.edit()
                        .putFloat(getString(R.string.pref_time_on_key), totalTimeOn)
                        .apply();
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
