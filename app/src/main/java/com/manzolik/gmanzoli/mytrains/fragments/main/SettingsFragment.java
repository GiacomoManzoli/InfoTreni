package com.manzolik.gmanzoli.mytrains.fragments.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;


import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NOTIFICATION_DAYS= "notification_days";
    public static final String NOTIFICATION_LOCATION_FILTERING = "location_filtering";
    public static final String REMINDER_SORTING = "reminder_sorting";


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);

        // Disabilita le preferenze per i giorni della settimana se le notifiche sono disabilitate
        boolean notificationsEnabled = getPreferenceScreen()
                .getSharedPreferences()
                .getBoolean(NOTIFICATION_ENABLED, false);

        if (BuildConfig.DEBUG) Log.d(TAG, String.format("Notifiche abilitate: %s%n", notificationsEnabled));

        Preference dowPref = findPreference(NOTIFICATION_DAYS);
        dowPref.setEnabled(notificationsEnabled);
        Preference locPref = findPreference(NOTIFICATION_LOCATION_FILTERING);
        locPref.setEnabled(notificationsEnabled);
    }


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        boolean gotLocalizationPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        switch (key) {
            case NOTIFICATION_ENABLED:
                boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATION_ENABLED, false);

                // Disabilita le preferenze per i giorni della settimana se le notifiche sono disabilitate
                Preference dowPref = findPreference(NOTIFICATION_DAYS);
                dowPref.setEnabled(notificationsEnabled);
                Preference locPref = findPreference(NOTIFICATION_LOCATION_FILTERING);
                locPref.setEnabled(notificationsEnabled);
            /* Le notifiche vengono abilite o disabilitate quando
            * viene distrutta/creata MainActivity.
            */
                break;
            case NOTIFICATION_LOCATION_FILTERING:
                boolean geofilteringEnabled = sharedPreferences.getBoolean(NOTIFICATION_LOCATION_FILTERING, false);
                if (geofilteringEnabled) {
                    if (gotLocalizationPermission) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Ho i permessi");
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                }
                break;
            case REMINDER_SORTING:
                boolean reminderSortingEnabled = sharedPreferences.getBoolean(NOTIFICATION_LOCATION_FILTERING, false);
                if (reminderSortingEnabled) {
                    if (gotLocalizationPermission) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Ho i permessi");
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Ho ottenuto i permessi");
            }
        } else {
            if (BuildConfig.DEBUG) Log.e(TAG, "NON Ho ottenuto i permessi");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}