package com.manzolik.gmanzoli.mytrains.data.db;

import android.provider.BaseColumns;

abstract class StationTable implements BaseColumns {
    static final String TABLE_NAME = "stations";
    static final String NAME = "name";
    static final String CODE = "code";
    static final String REGION = "region";
    static final String REGION_CODE = "region_code";
    static final String CITY = "city";
    static final String LATITUDE = "lat";
    static final String LONGITUDE = "lon";

    static final String[] ALL_COLUMNS = {
            StationTable._ID,
            StationTable.CODE,
            StationTable.NAME,
            StationTable.REGION,
            StationTable.REGION_CODE,
            StationTable.CITY,
            StationTable.LATITUDE,
            StationTable.LONGITUDE };

    static final String CREATE_SQL = "CREATE TABLE "+StationTable.TABLE_NAME+ "("
            + StationTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + StationTable.NAME + " TEXT NOT NULL, "
            + StationTable.CODE + " TEXT UNIQUE NOT NULL, "
            + StationTable.REGION + " TEXT NOT NULL, "
            + StationTable.REGION_CODE + " INT NOT NULL, "
            + StationTable.CITY + " TEXT NOT NULL, "
            + StationTable.LATITUDE + " REAL NOT NULL, "
            + StationTable.LONGITUDE + " REAL NOT NULL)";

}