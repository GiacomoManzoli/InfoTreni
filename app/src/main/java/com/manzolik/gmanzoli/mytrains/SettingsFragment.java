package com.manzolik.gmanzoli.mytrains;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;


import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NOTIFICATION_DAYS= "notification_days";
    public static final String NOTIFICATION_LOCATION_FILTERING = "location_filtering";


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
        if (key.equals(NOTIFICATION_ENABLED)) {
            boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATION_ENABLED, false);

            // Disabilita le preferenze per i giorni della settimana se le notifiche sono disabilitate
            Preference dowPref = findPreference(NOTIFICATION_DAYS);
            dowPref.setEnabled(notificationsEnabled);
            Preference locPref = findPreference(NOTIFICATION_LOCATION_FILTERING);
            locPref.setEnabled(notificationsEnabled);
            /* Le notifiche vengono abilite o disabilitate quando
            * viene distrutta/creata MainActivity.
            */
        } else if (key.equals(NOTIFICATION_LOCATION_FILTERING)) {
            boolean geofilteringEnabled = sharedPreferences.getBoolean(NOTIFICATION_LOCATION_FILTERING, false);
            boolean gotPermission = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            if (geofilteringEnabled) {
                if (gotPermission){
                    if (BuildConfig.DEBUG) Log.d(TAG, "Ho i permessi");
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
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