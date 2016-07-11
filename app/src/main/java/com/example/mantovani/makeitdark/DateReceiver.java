package com.example.mantovani.makeitdark;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Mantovani on 10-Jul-16.
 */
public class DateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPrefs.edit()
                .clear()
                .putLong(context.getString(R.string.pref_last_unlock_key), System.currentTimeMillis())
                .putLong(context.getString(R.string.pref_last_lock_key), System.currentTimeMillis())
                .putLong(context.getString(R.string.pref_time_on_key), 1)
                .putLong(context.getString(R.string.pref_time_off_key), 0)
                .commit();
    }
}
