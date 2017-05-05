package com.manzolik.gmanzoli.mytrains.data.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;

import java.util.ArrayList;
import java.util.List;

public class StationDAO {
    private static final String TAG = StationDAO.class.getSimpleName();
    private MyTrainsDatabaseHelper mDbHelper;

    public StationDAO(Context context) {
        mDbHelper = MyTrainsDatabaseHelper.getInstance(context);
    }

    /*
    * Trova una stazione utilizzando il codice
    * */
    @Nullable
    public Station getStationFromCode(String code) {
        // NOTA: il codice in formato stringa identifica in modo univoco una stazione
        if (BuildConfig.DEBUG) Log.v(TAG, "getStationFromCode " + code);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(StationTable.TABLE_NAME,
                StationTable.ALL_COLUMNS,
                StationTable.CODE + "=?",
                new String[]{code},
                null, null, null);

        Station result = null;
        if (c.getCount() != 0 && c.moveToFirst()){
            result = buildStationFromCursor(c);
        }
        c.close();
        return result;
    }

    /*
    * Trova una stazione utilizzando l'id
    * */
    @Nullable
    public Station getStationFromId(int id) {
        if (BuildConfig.DEBUG) Log.v(TAG, "getStationFromId " + String.valueOf(id));
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(StationTable.TABLE_NAME, StationTable.ALL_COLUMNS, StationTable._ID + "=?", new String[]{Integer.toString(id)}, null, null, null);

        Station result = null;
        if (c.getCount() != 0 && c.moveToFirst()){
            result = buildStationFromCursor(c);
        }
        c.close();
        return result;
    }

    /*
    * Trova una stazione utilizzando il nome (anche match parziale)
    * */
    @Nullable
    public Station getStationFromName(String stationName) {
        if (BuildConfig.DEBUG) Log.v(TAG, "getStationFromName " + stationName);
        // NOTA: Non ci sono stazioni con lo stesso nome.
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String query = "SELECT * FROM "+StationTable.TABLE_NAME+" WHERE "+StationTable.NAME+" LIKE '%"+stationName+"%' COLLATE NOCASE;";
        Cursor c = db.rawQuery(query, null);

        Station result = null;
        if (c.getCount() != 0 && c.moveToFirst()){
            result = buildStationFromCursor(c);
        }

        c.close();
        return result;
    }

    /*
    * Autocompletamento del nome della stazione. Trova il nome di tutte le stazioni
    * che iniziano con `stationName`
    * */
    @NonNull
    public List<String> findStationsNameByName(String stationName){
        if (BuildConfig.DEBUG) Log.v(TAG, "findStationByName " + stationName);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<String> results = new ArrayList<>();

        //Cursor c = db.query(StationEntry.TABLE_NAME, projection, StationEntry.NAME + "LIKE ?", new String[]{"%"+stationName+"%"}, null, null, null);
        String query = "SELECT "+StationTable.NAME+" FROM "+StationTable.TABLE_NAME+" WHERE "+StationTable.NAME+" LIKE '"+stationName+"%' COLLATE NOCASE ORDER BY "+StationTable.NAME+" ASC;";
        Cursor c = db.rawQuery(query,null);

        if (c.getCount() != 0 && c.moveToFirst()){
            do {
                String name = c.getString(c.getColumnIndex(StationTable.NAME));
                results.add(name);
            } while (c.moveToNext());
        }

        c.close();
        return results;
    }

    /*
    * Crea una stazione a partire da un cursore contenente tutte le colonne della
    * tabella StationTable
    * */
    @NonNull
    private Station buildStationFromCursor(Cursor c) {
        String name = c.getString(c.getColumnIndex(StationTable.NAME));
        String region = c.getString(c.getColumnIndex(StationTable.REGION));
        int region_code = c.getInt(c.getColumnIndex(StationTable.REGION_CODE));
        String city = c.getString(c.getColumnIndex(StationTable.CITY));
        double lat = c.getDouble(c.getColumnIndex(StationTable.LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(StationTable.LONGITUDE));
        int id = c.getInt(c.getColumnIndex(StationTable._ID));
        String code = c.getString(c.getColumnIndex(StationTable.CODE));
        return new Station(id, name, code, region, region_code, city, lat, lon);
    }
}
