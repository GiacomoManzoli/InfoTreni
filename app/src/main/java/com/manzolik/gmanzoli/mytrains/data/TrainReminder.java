package com.manzolik.gmanzoli.mytrains.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class TrainReminder {

    private final int id;
    private final Train train;
    private final Calendar startTime;
    private final Calendar endTime;
    private final Station targetStaion;

    public TrainReminder(int id, Train train, Calendar startTime, Calendar endTime, Station targetStaion) {
        this.id = id;
        this.train = train;
        this.startTime = startTime;
        this.endTime = endTime;
        this.targetStaion = targetStaion;
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

    public Calendar getEndTime() {
        return endTime;
    }

    public Station getTargetStaion() {
        return targetStaion;
    }

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

}
