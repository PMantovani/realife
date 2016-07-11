package com.example.mantovani.makeitdark;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Mantovani on 06-Jul-16.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // User turned screen on
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            long lastScreenUnlock = System.currentTimeMillis();
            long totalTimeOff = sharedPrefs.getLong(context.getString(R.string.pref_time_off_key), 0);
            long lastScreenLock = sharedPrefs.getLong(context.getString(R.string.pref_last_lock_key),
                    System.currentTimeMillis());
            totalTimeOff += (lastScreenUnlock - lastScreenLock);
            sharedPrefs.edit()
                    .putLong(context.getString(R.string.pref_time_off_key), totalTimeOff)
                    .putLong(context.getString(R.string.pref_last_unlock_key), lastScreenUnlock)
                    .apply();

            Intent serviceIntent = new Intent(context, MonitorService.class);
            context.startService(serviceIntent);

            Toast.makeText(context, "DEU BOAAAAA", Toast.LENGTH_LONG).show();
        }
    }
}