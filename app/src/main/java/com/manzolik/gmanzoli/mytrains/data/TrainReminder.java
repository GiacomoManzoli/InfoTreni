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
            double distance = Math.pow(s.getLatitude() - location.getLatitude(), 2) +
                    Math.pow(s.getLongitude() - location.getLongitude(), 2);
            distance = Math.sqrt(distance);
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

        List<TrainReminder> trainList = new ArrayList<>();

        if (BuildConfig.DEBUG){
            Log.d(TAG, "Fitro i reminder per shouldShow");
            Log.d(TAG, "Reminder pre-filtering: "+String.valueOf(reminders.size()));
        }
        List<TrainReminder> result = new ArrayList<>();

        for (TrainReminder tr: reminders) {
            if (tr.shouldShowReminder(currentTime) && !trainInReminderList(tr.getTrain().getCode(), trainList)) {
                result.add(tr);
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Reminder post-filtering: "+String.valueOf(result.size()));

        return result;
    }

    private static boolean trainInReminderList(String trainCode, List<TrainReminder> reminders){
        for(int i = 0; i < reminders.size(); i++){
            if (reminders.get(i).getTrain().getCode().equals(trainCode)){
                return  true;
            }
        }
        return false;
    }
}
