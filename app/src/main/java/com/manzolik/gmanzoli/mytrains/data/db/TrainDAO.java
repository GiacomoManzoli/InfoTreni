package com.manzolik.gmanzoli.mytrains.data.db;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;

public class TrainDAO{

    private static final String TAG = TrainDAO.class.getSimpleName();
    private MyTrainsDatabaseHelper mDbHelper;
    private final Context mContext;

    public TrainDAO(Context context) {
        mContext = context;
        mDbHelper = MyTrainsDatabaseHelper.getInstance(context);
    }

    /*
    * Ottiene il treno (se presente) identificato dal codice e dalla stazione di partenza.
    * NOTA: il codice del treno non identifica in modo univico la tratta, serve anceh
    * il codice della stazione di partenza
    * */
    @Nullable
    public Train getTrainFromCode(String trainCode, String stationCode){
        if (BuildConfig.DEBUG) Log.d(TAG, "getTrainFromCode " + trainCode + " " + stationCode);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(TrainTable.TABLE_NAME,TrainTable.ALL_COLUMNS,TrainTable.CODE+"=?",new String[]{trainCode}, null, null, null);

        Train result = null;
        if (c.getCount() != 0 && c.moveToFirst()){
            StationDAO stationDAO = new StationDAO(mContext);
            do {
                /* Filtro i treni trovati per stazione di partenza */
                int stationId = c.getInt(c.getColumnIndex(TrainTable.DEPARTURE_STATION));
                Station station = stationDAO.getStationFromId(stationId);
                if (station != null && station.getCode().equals(stationCode)) {
                    result = buildTrainFromCursor(c, station);
                }
            } while (result == null && c.moveToNext());
        }
        c.close();
        return result;
    }

    /*
     * Ottiene il treno (se presente) identificato dall'id.
     * */
    @Nullable
    public Train getTrainFromId(int trainId){
        if (BuildConfig.DEBUG) Log.v(TAG, "getTrainFromId " + String.valueOf(trainId));
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(TrainTable.TABLE_NAME,TrainTable.ALL_COLUMNS,TrainTable._ID+"=?",new String[]{Integer.toString(trainId)}, null, null, null);

        Train result = null;

        if (c.getCount() != 0 && c.moveToFirst()){
            StationDAO stationDAO = new StationDAO(mContext);
            int stationId = c.getInt(c.getColumnIndex(TrainTable.DEPARTURE_STATION));
            Station station = stationDAO.getStationFromId(stationId);
            if (station != null) {
                result = buildTrainFromCursor(c, station);
            }
        }

        c.close();
        return result;
    }

     /*
     * Se il treno non è presente nel database lo inserisce e ritorna l'oggetto treno con l'id corretto
     * altrimenti ritorna direttamente l'oggetto.
     * Questo perché vengono salvate in locale solo le tratte per le quali è prensete
     * un reminder.
     * */
     @Nullable
     public Train insertTrainIfNotExists(String code, int departureId) {
         if (BuildConfig.DEBUG) Log.v(TAG, "insertTrainIfNotExists " + code + " " + String.valueOf(departureId));
         StationDAO stationDAO = new StationDAO(mContext);
         Station depStation = stationDAO.getStationFromId(departureId);
         if (depStation != null) {
             Train t = getTrainFromCode(code, depStation.getCode());
             if (t != null) {
                 // Tratta presente nel database, ritorno l'id della riga relativa
                 return t;
             } else {
                 // Inserimento del treno nel database
                 SQLiteDatabase db = mDbHelper.getWritableDatabase();

                 ContentValues values = new ContentValues();
                 values.put(TrainTable.CODE, code);
                 values.put(TrainTable.DEPARTURE_STATION, departureId);
                 long newRowId = db.insert(TrainTable.TABLE_NAME, null, values);

                 Train result = null;
                 if (newRowId != -1) {
                     result = new Train((int) newRowId, code, depStation);
                 }
                 mDbHelper.close();
                 return result;
             }
         } else {
             if (BuildConfig.DEBUG) Log.e(TAG, "Stazione non presente nel database");
             return null;
         }
     }

    /*
    * Costruisce un treno a partire dal cursore e dalla stazione di partenza del treno
    * */
    @NonNull
    private Train buildTrainFromCursor(Cursor c, Station station) {
        int id = c.getInt(c.getColumnIndex(TrainTable._ID));
        String trainCode = c.getString(c.getColumnIndex(TrainTable.CODE));
        return new Train(id, trainCode, station);
    }

}
