package com.manzolik.gmanzoli.mytrains.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.SettingsFragment;

public class BootNotificationReceiver extends BroadcastReceiver{

    private static final String TAG = BootNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Device avviato");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_ENABLED, false);

        if (notificationEnabled) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Riavvio l'avviso delle notifiche");
            SchedulingAlarmReceiver.startRepeatingAlarm(context);
        }
    }
}
