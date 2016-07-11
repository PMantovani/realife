package com.example.mantovani.makeitdark;

import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by Mantovani on 10-Jul-16.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener{

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
