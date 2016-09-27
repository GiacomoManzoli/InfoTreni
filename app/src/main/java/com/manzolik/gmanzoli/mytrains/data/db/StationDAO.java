package com.manzolik.gmanzoli.mytrains.data.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.manzolik.gmanzoli.mytrains.data.Station;

import java.util.ArrayList;
import java.util.List;

public class StationDAO extends MyTrainsDatabaseHelper{

    public StationDAO(Context context) {
        super(context);

    }

    public Station getStationFromCode(String code) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                StationEntry._ID,
                StationEntry.NAME,
                StationEntry.REGION,
                StationEntry.REGION_CODE,
                StationEntry.CITY,
                StationEntry.LATITUDE,
                StationEntry.LONGITUDE };

        Cursor c = db.query(StationEntry.TABLE_NAME, projection, StationEntry.CODE + "=?", new String[]{code}, null, null, null);
        if (c.getCount() == 0){
            return null;
        }
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(StationEntry.NAME));
        String region = c.getString(c.getColumnIndex(StationEntry.REGION));
        int region_code = c.getInt(c.getColumnIndex(StationEntry.REGION_CODE));
        String city = c.getString(c.getColumnIndex(StationEntry.CITY));
        double lat = c.getDouble(c.getColumnIndex(StationEntry.LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(StationEntry.LONGITUDE));
        int id = c.getInt(c.getColumnIndex(StationEntry._ID));

        c.close();
        close();
        return new Station(id, name, code,region, region_code, city,lat,lon);
    }

    public Station getStationFromID(int id) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                StationEntry._ID,
                StationEntry.CODE,
                StationEntry.NAME,
                StationEntry.REGION,
                StationEntry.REGION_CODE,
                StationEntry.CITY,
                StationEntry.LATITUDE,
                StationEntry.LONGITUDE };

        Cursor c = db.query(StationEntry.TABLE_NAME, projection, StationEntry._ID + "=?", new String[]{Integer.toString(id)}, null, null, null);
        //Cursor c = db.rawQuery("SELECT * FROM "+StationEntry.TABLE_NAME+" WHERE "+StationEntry._ID+"="+Integer.toString(id), null);
        if (c.getCount() == 0){
            return null;
        }
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex(StationEntry.NAME));
        String region = c.getString(c.getColumnIndex(StationEntry.REGION));
        int region_code = c.getInt(c.getColumnIndex(StationEntry.REGION_CODE));
        String city = c.getString(c.getColumnIndex(StationEntry.CITY));
        double lat = c.getDouble(c.getColumnIndex(StationEntry.LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(StationEntry.LONGITUDE));
        String code = c.getString(c.getColumnIndex(StationEntry.CODE));

        c.close();
        close();
        return new Station(id, name, code,region, region_code, city,lat,lon);
    }

    public Station getStationFromName(String stationName) {
        SQLiteDatabase db = getReadableDatabase();



        //Cursor c = db.query(StationEntry.TABLE_NAME, projection, StationEntry.NAME + "LIKE ?", new String[]{"%"+stationName+"%"}, null, null, null);
        String query = "SELECT * FROM "+StationEntry.TABLE_NAME+" WHERE "+StationEntry.NAME+" LIKE '%"+stationName+"%' COLLATE NOCASE;";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() == 0){
            return null;
        }
        c.moveToFirst();

        String name = c.getString(c.getColumnIndex(StationEntry.NAME));
        String region = c.getString(c.getColumnIndex(StationEntry.REGION));
        int region_code = c.getInt(c.getColumnIndex(StationEntry.REGION_CODE));
        String city = c.getString(c.getColumnIndex(StationEntry.CITY));
        double lat = c.getDouble(c.getColumnIndex(StationEntry.LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(StationEntry.LONGITUDE));
        String code = c.getString(c.getColumnIndex(StationEntry.CODE));
        int id = c.getInt(c.getColumnIndex(StationEntry._ID));

        c.close();
        close();
        return new Station(id, name, code,region, region_code, city,lat,lon);
    }

    public List<String> findStationsNameByName(String stationName){
        SQLiteDatabase db = getReadableDatabase();
        List<String> results = new ArrayList<>();


        //Cursor c = db.query(StationEntry.TABLE_NAME, projection, StationEntry.NAME + "LIKE ?", new String[]{"%"+stationName+"%"}, null, null, null);
        String query = "SELECT "+StationEntry.NAME+" FROM "+StationEntry.TABLE_NAME+" WHERE "+StationEntry.NAME+" LIKE '"+stationName+"%' COLLATE NOCASE;";
        Cursor c = db.rawQuery(query,null);
        if (c.getCount() == 0){
            return results;
        }
        if (c != null && c.moveToFirst()) {

            do {
                String name = c.getString(c.getColumnIndex(StationEntry.NAME));
                //String region = c.getString(c.getColumnIndex(StationEntry.REGION));
                //int region_code = c.getInt(c.getColumnIndex(StationEntry.REGION_CODE));
                //String city = c.getString(c.getColumnIndex(StationEntry.CITY));
                //double lat = c.getDouble(c.getColumnIndex(StationEntry.LATITUDE));
                //double lon = c.getDouble(c.getColumnIndex(StationEntry.LONGITUDE));
                //String code = c.getString(c.getColumnIndex(StationEntry.CODE));
                //int id = c.getInt(c.getColumnIndex(StationEntry._ID));
                //results.add(new Station(id, name, code,region, region_code, city,lat,lon));
                results.add(name);
            }while (c.moveToNext());
        }
        c.close();
        close();
        return results;
    }
}
