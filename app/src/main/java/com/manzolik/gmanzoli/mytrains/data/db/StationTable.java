package com.manzolik.gmanzoli.mytrains.data.db;


abstract class StationTable {
    static final String TABLE_NAME = "stations";
    static final String _ID = "station_id";
    static final String NAME = "station_name";
    static final String CODE = "station_code";
    static final String REGION = "station_region";
    static final String REGION_CODE = "region_code";
    static final String CITY = "station_city";
    static final String LATITUDE = "station_lat";
    static final String LONGITUDE = "station_lon";
    static final String MAINTENANCE_REQUIRED = "station_maintenance";
    static final String FAVORITE = "station_favorite";

    static final String[] ALL_COLUMNS = {
            StationTable._ID,
            StationTable.CODE,
            StationTable.NAME,
            StationTable.REGION,
            StationTable.REGION_CODE,
            StationTable.CITY,
            StationTable.LATITUDE,
            StationTable.LONGITUDE,
            StationTable.FAVORITE,
            StationTable.MAINTENANCE_REQUIRED
    };

    static final String CREATE_SQL = "CREATE TABLE "+StationTable.TABLE_NAME+ "("
            + StationTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + StationTable.NAME + " TEXT NOT NULL, "
            + StationTable.CODE + " TEXT UNIQUE NOT NULL, "
            + StationTable.REGION + " TEXT NOT NULL, "
            + StationTable.REGION_CODE + " INT NOT NULL, "
            + StationTable.CITY + " TEXT NOT NULL, "
            + StationTable.LATITUDE + " REAL NOT NULL, "
            + StationTable.LONGITUDE + " REAL NOT NULL, "
            + StationTable.FAVORITE + " INTEGER NOT NULL, "
            + StationTable.MAINTENANCE_REQUIRED + " INTEGER NOT NULL);";

}