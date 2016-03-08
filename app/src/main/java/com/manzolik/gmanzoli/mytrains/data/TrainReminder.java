package com.manzolik.gmanzoli.mytrains.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by gmanzoli on 26/02/16.
 */
public class TrainReminder {

    private final Train train;
    private final Calendar startTime;
    private final Calendar endTime;
    private final Station targetStaion;

    public TrainReminder(Train train, GregorianCalendar startTime, GregorianCalendar endTime, Station targetStaion) {
        this.train = train;
        this.startTime = startTime;
        this.endTime = endTime;
        this.targetStaion = targetStaion;
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


    public static List<TrainReminder> getDebugList(){
        List<TrainReminder> trainReminderList = new ArrayList<>();
        // TrainReminder
        Station bolognaCentrale = new Station("Bologna C.le", "S05043");
        Station veneziaSL = new Station("Venezia S.L.", "S02593");
        Station rovigo = new Station("Rovigo", "S05706");
        Station padova = new Station("Padova", "S02581");

        // Venezia --> Bologna
        trainReminderList.add(new TrainReminder(new Train(2233, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,11,40),new GregorianCalendar(2016,3,1,12,20), padova));
        trainReminderList.add(new TrainReminder(new Train(2235, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,12,40),new GregorianCalendar(2016,3,1,13,20), padova));
        trainReminderList.add(new TrainReminder(new Train(2237, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,13,40),new GregorianCalendar(2016,3,1,14,20), padova));
        trainReminderList.add(new TrainReminder(new Train(2239, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,14,40),new GregorianCalendar(2016,3,1,15,50), padova));
        trainReminderList.add(new TrainReminder(new Train(2241, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,15,40),new GregorianCalendar(2016,3,1,16,20), padova));
        trainReminderList.add(new TrainReminder(new Train(2243, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,16,40),new GregorianCalendar(2016,3,1,17,20), padova));
        trainReminderList.add(new TrainReminder(new Train(2245, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,17,40),new GregorianCalendar(2016,3,1,18,20), padova));

        // Bologna --> Venezia
        trainReminderList.add(new TrainReminder(new Train(2222, bolognaCentrale, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,6,20),new GregorianCalendar(2016,3,1,7,15), rovigo));
        trainReminderList.add(new TrainReminder(new Train(2224, bolognaCentrale, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,7,20),new GregorianCalendar(2016,3,1,8,15), rovigo));
        trainReminderList.add(new TrainReminder(new Train(2226, bolognaCentrale, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,8,20),new GregorianCalendar(2016,3,1,9,15), rovigo));
        trainReminderList.add(new TrainReminder(new Train(2228, bolognaCentrale, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,9,20),new GregorianCalendar(2016,3,1,10,15), rovigo));
        trainReminderList.add(new TrainReminder(new Train(2230, bolognaCentrale, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,10,20),new GregorianCalendar(2016,3,1,11,15), rovigo));
        trainReminderList.add(new TrainReminder(new Train(2232, bolognaCentrale, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,11,20),new GregorianCalendar(2016,3,1,12,15), rovigo));

        // Debug
        trainReminderList.add(new TrainReminder(new Train(9455, veneziaSL, "ES*", Calendar.getInstance()),new GregorianCalendar(2016,3,1,23,10),new GregorianCalendar(2016,3,1,1,0), padova));
        trainReminderList.add(new TrainReminder(new Train(2233, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,0,0),new GregorianCalendar(2016,3,1,1,0), padova));
        trainReminderList.add(new TrainReminder(new Train(2233, veneziaSL, "RGV", Calendar.getInstance()),new GregorianCalendar(2016,3,1,0,0),new GregorianCalendar(2016,3,1,1,0), padova));


        return trainReminderList;
    }
}
