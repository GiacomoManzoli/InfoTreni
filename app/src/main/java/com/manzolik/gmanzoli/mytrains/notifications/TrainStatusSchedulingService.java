package com.manzolik.gmanzoli.mytrains.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.MainActivity;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.SettingsFragment;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainReminderStatusService;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TrainStatusSchedulingService extends IntentService
    implements TrainReminderStatusService.TrainReminderStatusServiceListener {

    private static final String TAG = TrainStatusSchedulingService.class.getSimpleName();

    private Intent mIntent;

    public TrainStatusSchedulingService() {
        super("SchedulingService");
    }


    // Metodo invocato circa ogni minuto per la generazione delle notifiche
    // Se le notifiche sono disabilitate, questo metodo non viene mai invocato
    @Override
    protected void onHandleIntent(Intent intent) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Gestisco l'intent");
        mIntent = intent;
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
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
        if (! notificationsDay.contains(dayOfWeek)){
            return;
        }

        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(getApplicationContext());
        List<TrainReminder> reminders = trainReminderDAO.getAllReminders();

        TrainReminderStatusService tss = new TrainReminderStatusService();
        tss.getTrainStatusList(reminders,this);
    }

    /*
    * TrainReminderStatusService.TrainReminderStatusServiceListener
    * */

    @Override
    public void onTrainReminderStatusServiceSuccess(List<TrainStatus> statuses) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Recuperato stato dei treni");
        for (TrainStatus ts: statuses){
            String title = "InfoTreni - " + ts.getTrainDescription();
            String trainCode = ts.getTrainCode();

            String message;
            if (true || !ts.isTargetPassed()){ // Se il punto di interesse è già passato non ha senso mettere la notifica
                if (ts.isSuppressed()){
                    message = "Treno soppresso";
                } else if (ts.isDeparted()){
                    if (ts.getDelay() > 0){
                        int delay = ts.getDelay();
                        if (delay == 1){
                            message = "Il treno viaggia con un minuto di ritardo";
                        }else {
                            message = String.format(Locale.getDefault(), "Il treno viaggia con %d minuti di ritardo", delay);
                        }
                    } else if (ts.getDelay() == 0) {
                        message = "Il treno è in orario";
                    } else {
                        int delay = -1 * ts.getDelay();
                        if (delay == 1){
                            message = "Il treno viaggia con un minuto di anticipo";
                        }else {
                            message = String.format(Locale.getDefault(), "Il treno viaggia con %d minuti di anticipo", delay);
                        }
                    }
                } else {
                    message = "Il treno non è ancora partito";
                    if (ts.getDelay() > 0){
                        message+= String.format(Locale.getDefault(), ", ritardo previsto di %d minuti", ts.getDelay());
                    }
                }
                sendNotification(title,message, trainCode);
            }

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

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_alt)
                        .setContentTitle(title)
                        .setGroup("statuses")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(trainCode.hashCode(), mBuilder.build());
    }


}
