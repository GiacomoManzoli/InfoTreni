package com.manzolik.gmanzoli.mytrains.data;

import java.io.Serializable;
import java.util.Calendar;

public class TrainReminder implements Serializable {

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
}
