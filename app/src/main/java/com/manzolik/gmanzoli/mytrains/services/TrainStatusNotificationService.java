package com.manzolik.gmanzoli.mytrains.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.MainActivity;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.fragments.main.SettingsFragment;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.data.http.TrainReminderStatusService;
import com.manzolik.gmanzoli.mytrains.receivers.SchedulingAlarmReceiver;
import com.manzolik.gmanzoli.mytrains.utils.LocationUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TrainStatusNotificationService extends IntentService
        implements TrainReminderStatusService.TrainReminderStatusServiceListener {

    private static final String TAG = TrainStatusNotificationService.class.getSimpleName();

    private static final String NOTIFICATION_SEPARATOR = "---";

    private Intent mIntent;

    public TrainStatusNotificationService() {
        super("SchedulingService");
    }


    // Metodo invocato circa ogni minuto per la generazione delle notifiche
    // Se le notifiche sono disabilitate, questo metodo non viene mai invocato
    @Override
    protected void onHandleIntent(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Gestisco l'intent");
        mIntent = intent;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        // Se il giorno della settimana non è abilito, termino l'esecuzione senza
        // andare ad effettuare le chiamate alle API
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Variabile che contiene il codice dei giorni della settimana in cui mostrare le notifiche
        // Domenica = 1, Lunedì = 2 ... Sabato = 7
        Set<String> notificationsDay = sharedPref.getStringSet(SettingsFragment.NOTIFICATION_DAYS, null);

        // Per questioni di praticità converto il numero in stringa
        String dayOfWeek = String.format(Locale.getDefault(), "%d", Calendar.getInstance().get(Calendar.DAY_OF_WEEK));

        // Se il giorno corrente NON è contenuto nel set dei giorni in cui mostrare le notifiche
        // evito la chiamata alle API
        if (notificationsDay != null && !notificationsDay.contains(dayOfWeek)) {
            return;
        }

        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(getApplicationContext());
        List<TrainReminder> reminders = trainReminderDAO.getAllReminders();

        reminders = TrainReminder.filterByShouldShow(reminders);
        reminders = TrainReminder.filterByRemoveDuplicates(reminders);
        // ^ se ci sono più reminder per lo stesso treno che devono ancora essere visualizzati
        // tiene quello relativo alla stazione più vicina alla stazione di partenza del treno

        boolean geofilteringEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_LOCATION_FILTERING, false);
        TrainReminderStatusService tss = new TrainReminderStatusService();

        if (geofilteringEnabled) {
            Location lastLocation = LocationUtils.getLastLocation(getApplicationContext());
            /* lastLocation = null anche se la geolocalizzazione è disabilitata a sistema */
            if (lastLocation != null) {
                reminders = TrainReminder.filterByLocation(reminders, lastLocation);
            } else {
                if (BuildConfig.DEBUG) Log.d(TAG, "Location: null - non applico il filtro");
            }
            tss.getTrainStatusList(reminders,this); // Eseguo comunque la richiesta senza filtrare
            // perché la geolocalizzazione potrebbe essere disattivata
        } else {
            tss.getTrainStatusList(reminders,this);
        }
    }

    /*
    * TrainReminderStatusService.TrainReminderStatusServiceListener
    * */

    @Override
    public void onTrainReminderStatusServiceSuccess(List<TrainStatus> statuses) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Recuperato stato dei treni");

        String title = "InfoTreni";
        String notificationMessage = "";
        for (TrainStatus ts: statuses){

            String trainCode = ts.getTrainCode();
            String message;
            if (!ts.isTargetPassed()){ // Se il punto di interesse è già passato non ha senso mettere la notifica
                if (ts.getTrainStatusInfo() == TrainStatus.TrainStatusInfo.STATUS_SUPPRESSED){
                    message = "Soppresso";
                } else if (ts.isDeparted()){
                    if (ts.getDelay() > 0){
                        int delay = ts.getDelay();
                        message = String.format(Locale.getDefault(), "Ritardo %d'", delay);
                    } else if (ts.getDelay() == 0) {
                        message = "In orario";
                    } else {
                        int delay = -1 * ts.getDelay();
                        message = String.format(Locale.getDefault(), "Anticipo %d", delay);
                    }
                } else {
                    message = "Non partito";
                    if (ts.getDelay() > 0){
                        message+= String.format(Locale.getDefault(), ", ritardo previsto %d'", ts.getDelay());
                    }
                }
                notificationMessage += trainCode+ " - "+ message + NOTIFICATION_SEPARATOR;
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Messaggio notifica: "+ notificationMessage );
        if (!notificationMessage.equals("")) {
            notificationMessage = notificationMessage.substring(0, notificationMessage.lastIndexOf(NOTIFICATION_SEPARATOR));
            sendNotification(title,notificationMessage, "");
        }
        SchedulingAlarmReceiver.completeWakefulIntent(mIntent);
    }

    @Override
    public void onTrainReminderStatusServiceFailure(Exception e) {
        Log.e(TAG, e.getMessage());
        SchedulingAlarmReceiver.completeWakefulIntent(mIntent);
    }


    private void sendNotification(String title, String msg, String trainCode) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        String shortMessage = msg.replace(NOTIFICATION_SEPARATOR, " -- ");
        String longMessage = msg.replace(NOTIFICATION_SEPARATOR, "\n");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
            // il \n da problemi con le notifiche di Android N
            longMessage = shortMessage;
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_alt)
                        .setContentTitle(title)
                        .setContentText(shortMessage)
                        .setGroup("statuses")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(longMessage));

        builder.setContentIntent(contentIntent);
        mNotificationManager.notify(trainCode.hashCode(), builder.build());
    }


}
