package com.manzolik.gmanzoli.mytrains.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainReminderDAO {

    private static final String TAG = TrainReminderDAO.class.getSimpleName();

    private final MyTrainsDatabaseHelper mDbHelper;
    private final Context mContext;

    public TrainReminderDAO(Context context) {
        mDbHelper = MyTrainsDatabaseHelper.getInstance(context);
        mContext = context;
    }


    /*
     * Carica tutti i reminder presenti nel database
     * */
    @NonNull
    public List<TrainReminder> getAllReminders(){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(TrainReminderTable.TABLE_NAME,
                TrainReminderTable.ALL_COLUMNS, null, null, null, null, null);
        //c = db.rawQuery("select * from "+TrainReminderEntry.TABLE_NAME,null);

        List<TrainReminder> trList = new ArrayList<>();

        while (c.moveToNext()){
            int trainId = c.getInt(c.getColumnIndex(TrainReminderTable.TRAIN));
            int stationId = c.getInt(c.getColumnIndex(TrainReminderTable.TARGET_STATION));
            StationDAO stationDAO = new StationDAO(mContext);
            Station station = stationDAO.getStationFromId(stationId);
            TrainDAO trainDAO = new TrainDAO(mContext);
            Train train = trainDAO.getTrainFromId(trainId);

            trList.add(buildTrainReminderFromCursor(c, train, station));
        }
        c.close();
        return trList;
    }

    /*
    * Caricamento dei reminder in modo asincrono
    * */
    public void getAllRemindersAsync(final OnGetReminderAsyncListener listener) {
        new AsyncTask<Void, Void, List<TrainReminder>>() {
            @Override
            protected List<TrainReminder> doInBackground(Void... params) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Caricamento dei reminder in background...");
                return TrainReminderDAO.this.getAllReminders();
            }

            @Override
            protected void onPostExecute(List<TrainReminder> reminders) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Caricamento dei reminder in background... completato");
                if (listener != null) {
                    listener.onGetReminders(reminders);
                }
            }
        }.execute();
    }
    /*
    * Inserisce un reminder a partire dai singoli valori
    * */
    public boolean insertReminder(String trainCode, int trainDepartureID, Calendar startTime, Calendar endTime, int targetStationID) {
        TrainDAO trainDAO = new TrainDAO(mContext);

        Train t = trainDAO.insertTrainIfNotExists(trainCode, trainDepartureID);
        if (t != null) {
            StationDAO stationDAO = new StationDAO(mContext);
            Station s = stationDAO.getStationFromId(targetStationID);

            if (s != null){
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(TrainReminderTable.TRAIN, t.getId());
                values.put(TrainReminderTable.TARGET_STATION, targetStationID);
                values.put(TrainReminderTable.START_TIME, startTime.getTimeInMillis());
                values.put(TrainReminderTable.END_TIME, endTime.getTimeInMillis());

                long newRowId = db.insert(TrainReminderTable.TABLE_NAME,null, values);
                return newRowId != -1;
            }
        }
        return false;
    }

    /*
    * Inserisce un reminder a partire dall'oggetto (il campo id non viene considerato)
    * */
    public boolean insertReminder(TrainReminder trainReminder) {
        return insertReminder(
                trainReminder.getTrain().getCode(),
                trainReminder.getTrain().getDepartureStation().getId(),
                trainReminder.getStartTime(),
                trainReminder.getEndTime(),
                trainReminder.getTargetStation().getId()
        );
    }

    /*
    * Salva sul database le modifiche subite dal reminder passato come parametro
    * */
    public boolean updateReminder(TrainReminder reminder) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateReminder " + reminder.toString());

        StationDAO stationDAO = new StationDAO(mContext);
        Station targetStation = stationDAO.getStationFromId(reminder.getTargetStation().getId());

        if (targetStation != null) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TrainReminderTable.START_TIME, reminder.getStartTime().getTimeInMillis());
            values.put(TrainReminderTable.END_TIME, reminder.getEndTime().getTimeInMillis());
            values.put(TrainReminderTable.TARGET_STATION, targetStation.getId());

            String whereClause = String.format(Locale.getDefault(), "%s = %d", TrainReminderTable._ID, reminder.getId());
            int rowsAffected = db.update(TrainReminderTable.TABLE_NAME, values, whereClause, null);
            return rowsAffected == 1;
        }
        return false;
    }

    /*
    * Cancella dal database il reminder passato come parametro
    * */
    public boolean deleteReminder(TrainReminder reminder) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsAffected = db.delete(TrainReminderTable.TABLE_NAME,
                TrainReminderTable._ID + "=?",
                new String[]{Integer.toString(reminder.getId())} );
        return rowsAffected == 1;
    }

    /*
    * Costruisce un reminder a partire dal cursore con la proiezione completa
    * */
    @NonNull
    private TrainReminder buildTrainReminderFromCursor(Cursor c, Train train, Station station) {
        int id = c.getInt(c.getColumnIndex(TrainReminderTable._ID));
        long startTimeMillis = c.getLong(c.getColumnIndex(TrainReminderTable.START_TIME));
        long endTimeMillis = c.getLong(c.getColumnIndex(TrainReminderTable.END_TIME));

        Calendar startTime = Calendar.getInstance();
        startTime.setTime(new Date(startTimeMillis));
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(new Date(endTimeMillis));

        return new TrainReminder(id, train, startTime, endTime, station);
    }

    public interface OnGetReminderAsyncListener {
        void onGetReminders(List<TrainReminder> reminders);
    }
}
