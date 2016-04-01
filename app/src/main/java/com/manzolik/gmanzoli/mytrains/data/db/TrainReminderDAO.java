package com.manzolik.gmanzoli.mytrains.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrainReminderDAO extends MyTrainsDatabaseHelper{
    private final Context context;

    public TrainReminderDAO(Context context) {
        super(context);
        this.context = context;

    }

    public List<TrainReminder> getAllReminders(){
        SQLiteDatabase db = getReadableDatabase();

        String[] proj = {
                TrainReminderEntry._ID,
                TrainReminderEntry.TRAIN,
                TrainReminderEntry.TARGET_STATION,
                TrainReminderEntry.START_TIME,
                TrainReminderEntry.END_TIME
        };
        Cursor c = db.query(TrainReminderEntry.TABLE_NAME,proj,null,null, null, null, null);
        //c = db.rawQuery("select * from "+TrainReminderEntry.TABLE_NAME,null);
        List<TrainReminder> trList = new ArrayList<>();



        while (c.moveToNext()){
            int id = c.getInt(c.getColumnIndex(TrainReminderEntry._ID));
            int trainId = c.getInt(c.getColumnIndex(TrainReminderEntry.TRAIN));
            int stationId = c.getInt(c.getColumnIndex(TrainReminderEntry.TARGET_STATION));
            long startTimeMillis = c.getLong(c.getColumnIndex(TrainReminderEntry.START_TIME));
            long endTimeMillis = c.getLong(c.getColumnIndex(TrainReminderEntry.END_TIME));

            Calendar startTime = Calendar.getInstance();
            startTime.setTime(new Date(startTimeMillis));
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(new Date(endTimeMillis));

            StationDAO stationDAO = new StationDAO(context);
            Station station = stationDAO.getStationFromID(stationId);
            TrainDAO trainDAO = new TrainDAO(context);
            Train train = trainDAO.getTrainFromID(trainId);
            TrainReminder tr = new TrainReminder(id, train, startTime, endTime, station);

            trList.add(tr);
        }

        c.close();
        close();
        return trList;
    }

    public boolean insertReminder(int trainCode, int trainDepartureID, Calendar startTime, Calendar endTime, int targetStationID) {
        TrainDAO trainDAO = new TrainDAO(context);

        int trainID = trainDAO.insertTrainIfNotExists(trainCode, trainDepartureID);

        StationDAO stationDAO = new StationDAO(context);
        Station s = stationDAO.getStationFromID(targetStationID);
        if (s != null){
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TrainReminderEntry.TRAIN, trainID);
            values.put(TrainReminderEntry.TARGET_STATION, targetStationID);
            values.put(TrainReminderEntry.START_TIME, startTime.getTimeInMillis());
            values.put(TrainReminderEntry.END_TIME, endTime.getTimeInMillis());

            long newRowId = db.insert(TrainReminderEntry.TABLE_NAME,null, values);

            close();
            return newRowId != -1;
        }
        return false;
    }

    public void deleteReminder(TrainReminder reminder) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TrainReminderEntry.TABLE_NAME, TrainReminderEntry._ID + "=?", new String[]{Integer.toString(reminder.getId())});
        db.close();
    }
}
