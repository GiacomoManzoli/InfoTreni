package com.manzolik.gmanzoli.mytrains;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.db.TrainReminderDAO;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusService;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusServiceCallback;

import java.util.List;

public class TrainStatusSchedulingService extends IntentService
    implements TrainStatusServiceCallback{

    public TrainStatusSchedulingService() {
        super("SchedulingService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        TrainReminderDAO trainReminderDAO = new TrainReminderDAO(getApplicationContext());
        List<TrainReminder> reminders = trainReminderDAO.getAllReminders();

        TrainStatusService tss = new TrainStatusService();
        tss.getTrainStatusList(this, reminders);
    }

    @Override
    public void trainStatusServiceCallbackSuccess(List<TrainStatus> statuses) {
        for (TrainStatus ts: statuses){
            String title = "MyTrains - " + ts.getTrainDescription();
            int code = ts.getTrainCode();

            String message="";
            if (!ts.isTargetPassed()){ // Se il punto di interesse è già passato non ha senso mettere la notifica
                if (ts.isDeparted()){
                    if (ts.getDelay() > 0){
                        int delay = ts.getDelay();
                        if (delay == 1){
                            message = String.format("Il treno viaggia con %d minuto di ritardo", delay);
                        }else {
                            message = String.format("Il treno viaggia con %d minuti di ritardo", delay);
                        }
                    } else if (ts.getDelay() == 0) {
                        message = "Il treno è in orario";
                    } else {
                        int delay = -1 * ts.getDelay();
                        if (delay == 1){
                            message = String.format("Il treno viaggia con %d minuto di anticipo", delay);
                        }else {
                            message = String.format("Il treno viaggia con %d minuti di anticipo", delay);
                        }
                    }
                } else {
                    message = "Il treno non è ancora partito";
                    if (ts.getDelay() > 0){
                        message+= String.format(", ritardo previsto di %d minuti", ts.getDelay());
                    }
                }
                sendNotification(title,message, code);
            }

        }
    }

    @Override
    public void trainStatusServiceCallbackFailure(Exception exc) {

    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String title, String msg, int id) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TrainStatusActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setGroup("statuses")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(id, mBuilder.build());
    }


}
