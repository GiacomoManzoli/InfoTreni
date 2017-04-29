package com.manzolik.gmanzoli.mytrains;

import android.content.SharedPreferences;
import android.os.Bundle;


import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.notifications.SchedulingAlarmReceiver;


public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NOTIFICATION_DAYS= "notification_days";

    private SchedulingAlarmReceiver mReceiver;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);

        // Disabilita le preferenze per i giorni della settimana se le notifiche sono disabilitate
        boolean notificatonsEnabled = getPreferenceScreen().getSharedPreferences().getBoolean(NOTIFICATION_ENABLED, false);
        if (BuildConfig.DEBUG) Log.d(TAG, String.format("Notifiche abilitate: %s%n", notificatonsEnabled));
        Preference dowPref = findPreference(NOTIFICATION_DAYS);
        dowPref.setEnabled(notificatonsEnabled);


        mReceiver = new SchedulingAlarmReceiver();
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


            /* Le notifiche vengono abilite o disabilitate quando viene distrutta/creata MainActivity.
            // Attiva o disattiva le notifiche in base a quello che ha selezionato l'utente
            if (notificationsEnabled) {
                mReceiver.startRepeatingAlarm(this.getActivity());
            } else {
                mReceiver.stopRepeatingAlarm(this.getActivity());
            }*/
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