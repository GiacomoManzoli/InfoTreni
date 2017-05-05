package com.manzolik.gmanzoli.mytrains.data.db;

import android.provider.BaseColumns;

abstract class TrainTable implements BaseColumns {
    static final String TABLE_NAME = "trains";
    static final String CODE = "code";
    static final String DEPARTURE_STATION = "departure_station_id";

    static final String[] ALL_COLUMNS = {
            TrainTable._ID,
            TrainTable.CODE,
            TrainTable.DEPARTURE_STATION
    };

    static final String CREATE_SQL = "CREATE TABLE "+TrainTable.TABLE_NAME+" ("+TrainTable._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            " "+TrainTable.CODE+" TEXT UNIQUE NOT NULL, " +
            " "+TrainTable.DEPARTURE_STATION+" INT NOT NULL," +
            " FOREIGN KEY ("+TrainTable.DEPARTURE_STATION+") REFERENCES "+StationTable.TABLE_NAME+" ("+StationTable._ID+"));";
}