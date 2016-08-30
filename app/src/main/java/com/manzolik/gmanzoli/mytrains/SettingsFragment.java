package com.manzolik.gmanzoli.mytrains;

import android.content.SharedPreferences;
import android.os.Bundle;


import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NOTIFICATION_DAYS= "notification_days";

    private SchedulingAlarmReceiver receiver;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);

        // Disabilita le preferenze per i giorni della settimana se le notifiche sono disabilitate
        boolean notificatonsEnabled = getPreferenceScreen().getSharedPreferences().getBoolean(NOTIFICATION_ENABLED, false);
        Preference dowPref = findPreference(NOTIFICATION_DAYS);
        dowPref.setEnabled(notificatonsEnabled);


        receiver= new SchedulingAlarmReceiver();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(NOTIFICATION_ENABLED)) {
            boolean notificatonsEnabled = sharedPreferences.getBoolean(NOTIFICATION_ENABLED, false);

            // Disabilita le preferenze per i giorni della settimana se le notifiche sono disabilitate
            Preference dowPref = findPreference(NOTIFICATION_DAYS);
            dowPref.setEnabled(notificatonsEnabled);

            // Attiva o disattiva le notifiche in base a quello che ha selezionato l'utente
            if (notificatonsEnabled) {
                receiver.enableNotifications(this.getActivity());
            } else {
                receiver.disableNotifications(this.getActivity());
            }
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