package com.manzolik.gmanzoli.mytrains.data;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrainReminder implements Serializable {

    private static final String TAG = TrainReminder.class.getSimpleName();
    private final int id;
    private final Train train;
    private Calendar startTime;
    private Calendar endTime;
    private Station targetStation;

    public TrainReminder(int id, Train train, Calendar startTime, Calendar endTime, Station targetStation) {
        this.id = id;
        this.train = train;
        this.startTime = startTime;
        this.endTime = endTime;
        this.targetStation = targetStation;
    }

    public int getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public Calendar getStartTime() {
        return startTime;
    }
    public void setStartTime(Calendar v) { this.startTime = v;}

    public Calendar getEndTime() {
        return endTime;
    }
    public void setEndTime(Calendar v) { this.endTime = v; }

    public Station getTargetStation() {
        return targetStation;
    }
    public void setTargetStation(Station v) { this.targetStation = v; }


    public boolean shouldShowReminder(Calendar currentTime){
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY );
        int currentMinute = currentTime.get(Calendar.MINUTE );
        int reminderStartHour = this.getStartTime().get(Calendar.HOUR_OF_DAY);
        int reminderStartMinute = this.getStartTime().get(Calendar.MINUTE);
        int reminderEndHour = this.getEndTime().get(Calendar.HOUR_OF_DAY);
        int reminderEndMinute = this.getEndTime().get(Calendar.MINUTE);

        int now = currentHour * 60 + currentMinute;
        int startTime = reminderStartHour*60 + reminderStartMinute;
        int endTime = reminderEndHour*60 + reminderEndMinute;

        if (startTime <= endTime) {
            return now >= startTime && now <= endTime;
        } else {
            return now >= startTime || now <= endTime;
        }

    }

    @Override
    public String toString() {
        return train.toString();
    }

    /*
    * Filtra la lista di reminder, ritornando solamente i reminder relativi alla stazione
    * più vicina alla località passata come parametro
    * */
    @NonNull
    public static List<TrainReminder> filterByLocation(List<TrainReminder> reminders, Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Fitro i reminder per location");
            Log.d(TAG, "Reminder pre-filtering: "+String.valueOf(reminders.size()));
        }
        List<TrainReminder> result = new ArrayList<>();
        double minDistance = Double.MAX_VALUE;
        for (TrainReminder tr: reminders) {
            Station s = tr.getTargetStation();
            double distance = s.distanceFromLocation(location);
            if (distance < minDistance) {
                result = new ArrayList<>();
                minDistance = distance;
                if (BuildConfig.DEBUG) Log.d(TAG, "Stazione più vicina: "+tr.getTargetStation().getName() + " distanza: " +String.valueOf(distance));
            }
            if (distance == minDistance) {
                result.add(tr);
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Reminder post-filtering: "+String.valueOf(result.size()));

        return result;
    }

    /*
    * Filtra la lista di reminder, ritornando solamente i reminder che ha senso mostrare
    * all'utente
    * */
    public static List<TrainReminder> filterByShouldShow(List<TrainReminder> reminders) {
        Calendar currentTime = Calendar.getInstance();
        if (BuildConfig.DEBUG){
            Log.d(TAG, "Fitro i reminder per shouldShow");
            Log.d(TAG, "Reminder pre-filtering: "+String.valueOf(reminders.size()));
        }
        List<TrainReminder> result = new ArrayList<>();

        for (TrainReminder tr: reminders) {

            if (tr.shouldShowReminder(currentTime)) {
                result.add(tr);
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Reminder post-filtering: "+String.valueOf(result.size()));
        return result;
    }

    /*
    * Filtra la lista di reminder, ritornando solamente i reminder che ha senso mostrare
    * all'utente
    * */
    public static List<TrainReminder> filterByRemoveDuplicates(List<TrainReminder> reminders) {
        if (BuildConfig.DEBUG){
            Log.d(TAG, "Fitro i reminder per filterByRemoveDuplicates");
            Log.d(TAG, "Reminder pre-filtering: "+String.valueOf(reminders.size()));
        }
        List<TrainReminder> result = new ArrayList<>();

        for (TrainReminder tr: reminders) {
            int trainPosition = trainInReminderList(tr.getTrain().getCode(), result);
            if (trainPosition != -1) {
                // Devo tenere il reminder relativo alla prima stazione delle due che verrà
                // presa dal treno
                TrainReminder otherReminder = result.get(trainPosition);
                double distance1 = tr.train.getDepartureStation().distanceFromStation(tr.getTargetStation());
                double distance2 = tr.train.getDepartureStation().distanceFromStation(otherReminder.getTargetStation());

                if (distance1 < distance2){
                    // Il target del reminder che volevo aggiungere alla lista è più vicino alla
                    // stazione di patenza del treno rispetto al target dell'altro reminder
                    result.remove(trainPosition);
                    result.add(tr);
                }

            } else {
                result.add(tr);
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Reminder post-filtering: "+String.valueOf(result.size()));
        return result;
    }

    private static int trainInReminderList(String trainCode, List<TrainReminder> reminders){
        for(int i = 0; i < reminders.size(); i++){
            if (reminders.get(i).getTrain().getCode().equals(trainCode)){
                return i;
            }
        }
        return -1;
    }
}
