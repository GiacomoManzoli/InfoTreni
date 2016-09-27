package com.manzolik.gmanzoli.mytrains.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.preference.PreferenceManager;

import com.manzolik.gmanzoli.mytrains.SettingsFragment;

import java.util.Calendar;


public class SchedulingAlarmReceiver extends WakefulBroadcastReceiver {
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, TrainStatusSchedulingService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    // Metodo che attiva le notifiche, controllando se nelle impostazioni queste sono attive
    // Il metodo non effettua il controllo del giorno, il quale viene fatto dalla classe
    // TrainStatusSchedulingService perché risulta complesso gestire il "cambio giorno"
    public void enableNotifications(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_ENABLED,false);
        System.out.printf("Notifiche abilitate: %s%n", notificationEnabled);
        // Se le notifiche non sono abilitate evita di configurare il timer
        if (! notificationEnabled){
            // Disattiva le notifiche se sono abilitate e termina l'esecuzione del metodo
            disableNotifications(context);
            return;
        }

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SchedulingAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 10000, alarmIntent);


    }

    public void disableNotifications(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

    }

}
