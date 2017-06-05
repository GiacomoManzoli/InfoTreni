package com.manzolik.gmanzoli.mytrains.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.fragments.main.SettingsFragment;
import com.manzolik.gmanzoli.mytrains.utils.NetworkUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String NETWORK_ALIVE = "network_alive";

    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // OnePlus X (Android Marshmellow) funziona correttamente
        // Emulatore Nexus 5X non viene ricevuta la notifica
        if (NetworkUtils.isNetworkConnected(context)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Connessione ad internet attivata");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            boolean notificationEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_ENABLED, false);

            if (notificationEnabled) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Riavvio l'avviso delle notifiche");
                SchedulingAlarmReceiver.startRepeatingAlarm(context);
            }

            // Notifica locale
            Intent restoreIntent = new Intent(NETWORK_ALIVE);
            LocalBroadcastManager.getInstance(context).sendBroadcast(restoreIntent);
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Connessione ad internet disattivata");
            SchedulingAlarmReceiver.stopRepeatingAlarm(context);
        }
    }


}
