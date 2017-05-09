package com.manzolik.gmanzoli.mytrains.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.services.TrainStatusNotificationService;
import com.manzolik.gmanzoli.mytrains.utils.NetworkUtils;

import java.util.Calendar;


public class SchedulingAlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = SchedulingAlarmReceiver.class.getSimpleName();


    // Crea l'intent che viene attivato quando scatta l'allarme
    private static PendingIntent createAlarmIntent(Context context) {
        Intent intent = new Intent(context, SchedulingAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Ricevuto intent dal SO");

        // Intent per attivare il service delle notifiche
        Intent service = new Intent(context, TrainStatusNotificationService.class);

        // Start the service, keeping the device awake while it is launching.
        if (NetworkUtils.isNetworkConnected(context)) {
            startWakefulService(context, service);
        } else {
            if (BuildConfig.DEBUG) Log.e(TAG, "Nessuna connessione ad internet");
            stopRepeatingAlarm(context);
        }
    }

    // Il metodo non effettua il controllo del giorno, il quale viene fatto dalla classe
    // TrainStatusNotificationService perché risulta complesso gestire il "cambio giorno"
    public static void startRepeatingAlarm(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Abilitazione dell'allarme periodico");
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), // quando triggerarlo
                10000, // ogni quanto far ripartire l'allarme TODO portare a 60 secondi per la versione definitiva
                createAlarmIntent(context));

    }

    public static void stopRepeatingAlarm(Context context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Disabilitazione dell'allarme periodico");
        // If the alarm has been set, cancel it.
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(createAlarmIntent(context));
    }

}
