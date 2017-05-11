package com.manzolik.gmanzoli.mytrains.data.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

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
    * Trova una stazione utilizzando il nome
    * */
    @Nullable
    public Station getStationFromName(String stationName) {
        if (BuildConfig.DEBUG) Log.v(TAG, "getStationFromName " + stationName);
        /*
        * NOTA: Non ci sono stazioni con lo stesso nome.
        * */
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String query = "SELECT * FROM "+StationTable.TABLE_NAME+" WHERE "+StationTable.NAME+" LIKE '"+stationName+"' COLLATE NOCASE LIMIT 1;";
        Cursor c = db.rawQuery(query, null);

        Station result = null;
        if (c.getCount() != 0 && c.moveToFirst()){
            result = buildStationFromCursor(c);
        }

        c.close();
        return result;
    }


    public void findStationsNameByNameAsync(final String stationName, final OnFindStationNameAsyncListener listener){
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Caricamento dei nomi in background...");
                return StationDAO.this.findStationsNameByName(stationName);
            }

            @Override
            protected void onPostExecute(List<String> names) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Caricamento dei nomi in background... completato");
                if (listener != null) {
                    listener.onFindStationName(names);
                }
            }
        }.execute();
    }

    /*
    * Cerca in modo asincrono la stazione più vicina alla location passata come parametro
    * */
    public void findNearestStationAsync(final Location location, final OnFindNearestStationAsyncListener listener) {
        new AsyncTask<Void, Void, Station>() {
            @Override
            protected Station doInBackground(Void... params) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Caricamento dei nomi in background...");
                List<Station> stations = StationDAO.this.getAllStations();
                // Scelgo una stazione a caso come stazione più vicina.
                Station nearestStation = stations.get(0);
                double minDistance = nearestStation.distanceFromLocation(location);

                for (Station s: stations) {
                    double currentDistance = s.distanceFromLocation(location);
                    if (currentDistance < minDistance){
                        nearestStation = s;
                        minDistance = currentDistance;
                    }
                }
                return nearestStation;
            }

            @Override
            protected void onPostExecute(Station nearestStation) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Caricamento dei nomi in background... completato");
                if (listener != null) {
                    listener.onFindNearestStation(nearestStation);
                }
            }
        }.execute();
    }



    /*
    * Autocompletamento del nome della stazione. Trova il nome di tutte le stazioni
    * che iniziano con `stationName`
    * */
    @NonNull
    private List<String> findStationsNameByName(String stationName){
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

    @Nullable
    public Station insertStation(@NonNull Station dummy) {
        Station result = getStationFromCode(dummy.getCode());
        if (result == null) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues(8);
            cv.put(StationTable.NAME, StringUtils.capitalizeString(dummy.getName().trim()));
            cv.put(StationTable.CODE, dummy.getCode());
            cv.put(StationTable.REGION, dummy.getRegion());
            cv.put(StationTable.REGION_CODE, dummy.getRegionCode());
            cv.put(StationTable.CITY, dummy.getCity());
            cv.put(StationTable.LATITUDE, dummy.getLatitude());
            cv.put(StationTable.LONGITUDE, dummy.getLongitude());
            cv.put(StationTable.MAINTENANCE_REQUIRED,
                    (dummy.isMaintenanceRequired())? 1 : 0); // Flag che segnala la mancanza di dati

            long lastId = db.insert(StationTable.TABLE_NAME, null, cv);
            if (lastId != -1) {
                return new Station((int)lastId,
                        dummy.getName(),
                        dummy.getCode(),
                        dummy.getRegion(),
                        dummy.getRegionCode(),
                        dummy.getCity(),
                        dummy.getLatitude(),
                        dummy.getLongitude());
            } else {
                if (BuildConfig.DEBUG) Log.e(TAG, "Errore nell'inserimento di "+ dummy.getCode());
                return null;
            }
        } else {
            return result;
        }
    }


    public void updateStation(Station betterStation) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(8);
        cv.put(StationTable.NAME, StringUtils.capitalizeString(betterStation.getName().trim()));
        cv.put(StationTable.CODE, betterStation.getCode());
        cv.put(StationTable.REGION, betterStation.getRegion());
        cv.put(StationTable.REGION_CODE, betterStation.getRegionCode());
        cv.put(StationTable.CITY, betterStation.getCity());
        cv.put(StationTable.LATITUDE, betterStation.getLatitude());
        cv.put(StationTable.LONGITUDE, betterStation.getLongitude());
        cv.put(StationTable.MAINTENANCE_REQUIRED,
                (betterStation.isMaintenanceRequired())? 1 : 0); // Flag che segnala la mancanza di dati

        long lastId = db.update(StationTable.TABLE_NAME, cv, StationTable._ID+"=?",
                new String[]{String.valueOf(betterStation.getId())});

        if (BuildConfig.DEBUG) {
            if (lastId != 0) {
                Log.d(TAG, "Aggiornamento avvenuto con successo! "+ betterStation.getCode());
            } else {
                Log.e(TAG, "Errore nell'inserimento di "+ betterStation.getCode());
            }
        }
    }


    @NonNull
    public List<Station> getAllStationsWhichNeedsMaintenace() {
        if (BuildConfig.DEBUG) Log.v(TAG, "getAllStations - maintenance");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Station> results = new ArrayList<>();

        String query = "SELECT * FROM "+StationTable.TABLE_NAME+" WHERE "+StationTable.MAINTENANCE_REQUIRED+"=1;";
        Cursor c = db.rawQuery(query,null);

        if (c.getCount() != 0 && c.moveToFirst()){
            do {
                results.add(buildStationFromCursor(c));
            } while (c.moveToNext());
        }
        c.close();
        return results;
    }

    @NonNull
    private List<Station> getAllStations() {
        if (BuildConfig.DEBUG) Log.v(TAG, "getAllStations");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Station> results = new ArrayList<>();

        String query = "SELECT * FROM "+StationTable.TABLE_NAME+";";
        Cursor c = db.rawQuery(query,null);

        if (c.getCount() != 0 && c.moveToFirst()){
            do {
                results.add(buildStationFromCursor(c));
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
    static Station buildStationFromCursor(Cursor c) {
        return buildStationFromCursor(c, "");
    }
    @NonNull
    static Station buildStationFromCursor(Cursor c, String prefix) {
        String name = c.getString(c.getColumnIndex(prefix+StationTable.NAME));
        String region = c.getString(c.getColumnIndex(prefix+StationTable.REGION));
        int region_code = c.getInt(c.getColumnIndex(prefix+StationTable.REGION_CODE));
        String city = c.getString(c.getColumnIndex(prefix+StationTable.CITY));
        double lat = c.getDouble(c.getColumnIndex(prefix+StationTable.LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(prefix+StationTable.LONGITUDE));
        int id = c.getInt(c.getColumnIndex(prefix+StationTable._ID));
        String code = c.getString(c.getColumnIndex(prefix+StationTable.CODE));

        return new Station(id, name, code, region, region_code, city, lat, lon);
    }



    public interface OnFindStationNameAsyncListener {
        void onFindStationName(List<String> names);
    }

    public interface OnFindNearestStationAsyncListener {
        void onFindNearestStation(Station station);
    }

}
