package com.manzolik.gmanzoli.mytrains.data.db;

abstract class TrainTable {
    static final String TABLE_NAME = "trains";
    static final String _ID = "train_id";
    static final String CODE = "train_code";
    static final String DEPARTURE_STATION = "train_departure_station_id";

    static final String[] ALL_COLUMNS = {
            TrainTable._ID,
            TrainTable.CODE,
            TrainTable.DEPARTURE_STATION
    };

    static final String CREATE_SQL = "CREATE TABLE "+TrainTable.TABLE_NAME+" ("+TrainTable._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            " "+TrainTable.CODE+" TEXT NOT NULL, " + // Il codice treno non è UNIQUE!
            " "+TrainTable.DEPARTURE_STATION+" INT NOT NULL," +
            " FOREIGN KEY ("+TrainTable.DEPARTURE_STATION+") REFERENCES "+StationTable.TABLE_NAME+" ("+StationTable._ID+"));";
}