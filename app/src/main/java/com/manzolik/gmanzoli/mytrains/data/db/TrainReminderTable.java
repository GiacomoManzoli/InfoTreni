package com.manzolik.gmanzoli.mytrains.data.db;

abstract class TrainReminderTable{
    static final String TABLE_NAME = "train_reminders";
    static final String _ID = "reminder_id";
    static final String TRAIN = "reminder_train_id";
    static final String START_TIME = "reminder_start_time";
    static final String END_TIME = "reminder_end_time";
    static final String TARGET_STATION = "reminder_target_station_id";

    static final String[] ALL_COLUMNS = {
            TrainReminderTable._ID,
            TrainReminderTable.TRAIN,
            TrainReminderTable.TARGET_STATION,
            TrainReminderTable.START_TIME,
            TrainReminderTable.END_TIME
    };

    static final String CREATE_SQL = "CREATE TABLE "+ TABLE_NAME+" ("+ _ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            " "+ TRAIN+" INT NOT NULL," +
            " "+ START_TIME+" INT NOT NULL," +
            " "+ END_TIME+" INT NOT NULL," +
            " "+ TARGET_STATION+" INT NOT NULL," +
            " FOREIGN KEY ("+ TRAIN+") REFERENCES "+ TrainTable.TABLE_NAME+" ("+TrainTable._ID+")," +
            " FOREIGN KEY ("+ TARGET_STATION+") REFERENCES "+StationTable.TABLE_NAME+" ("+StationTable._ID+"));";
}

