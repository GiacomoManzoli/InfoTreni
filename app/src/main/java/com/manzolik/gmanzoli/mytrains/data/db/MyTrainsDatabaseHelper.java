package com.manzolik.gmanzoli.mytrains.data.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MyTrainsDatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "my_trains.db";
    public static final int DATABASE_VERSION = 1;

    private final Context context;

    public MyTrainsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /* Mettere delle sottoclassi che rappresentano le tabelle
    * e nel DAO solmaente i metodi che interagiscono con queste
    * i DAO subclassano questa classe*/
    @Override
    public void onCreate(SQLiteDatabase db) {

        System.out.println("CREATING TABLE: "+ StationEntry.TABLE_NAME);
        db.execSQL(StationEntry.CREATE_SQL);
        addStationsData(db);

        System.out.println("CREATING TABLE: "+ TrainEntry.TABLE_NAME);
        db.execSQL(TrainEntry.CREATE_SQL);
        System.out.println("CREATING TABLE: "+ TrainReminderEntry.TABLE_NAME);
        db.execSQL(TrainReminderEntry.CREATE_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static abstract class TrainEntry implements BaseColumns {
        public static final String TABLE_NAME = "trains";
        public static final String CODE = "code";
        public static final String DEPARTURE_STATION = "departure_station_id";

        public static final String CREATE_SQL = "CREATE TABLE "+TrainEntry.TABLE_NAME+" ("+TrainEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                " "+TrainEntry.CODE+" INT UNIQUE NOT NULL, " +
                " "+TrainEntry.DEPARTURE_STATION+" INT NOT NULL," +
                " FOREIGN KEY ("+TrainEntry.DEPARTURE_STATION+") REFERENCES "+StationEntry.TABLE_NAME+" ("+StationEntry._ID+"));";
    }

    public static abstract class TrainReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "train_reminders";
        public static final String TRAIN = "train_id";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String TARGET_STATION = "target_station_id";
        public static final String CREATE_SQL = "CREATE TABLE "+TrainReminderEntry.TABLE_NAME+" ("+TrainReminderEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                " "+TrainReminderEntry.TRAIN+" INT NOT NULL," +
                " "+TrainReminderEntry.START_TIME+" INT NOT NULL," +
                " "+TrainReminderEntry.END_TIME+" INT NOT NULL," +
                " "+TrainReminderEntry.TARGET_STATION+" INT NOT NULL," +
                " FOREIGN KEY ("+TrainReminderEntry.TRAIN+") REFERENCES "+TrainDAO.TrainEntry.TABLE_NAME+" ("+TrainDAO.TrainEntry._ID+")," +
                " FOREIGN KEY ("+TrainReminderEntry.TARGET_STATION+") REFERENCES "+StationEntry.TABLE_NAME+" ("+StationEntry._ID+"));";
    }
    public static abstract class StationEntry implements BaseColumns {
        public static final String TABLE_NAME = "stations";
        public static final String NAME = "name";
        public static final String CODE = "code";
        public static final String REGION = "region";
        public static final String REGION_CODE = "region_code";
        public static final String CITY = "city";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "lon";
        public static final String CREATE_SQL = "CREATE TABLE stations ("+StationEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, code TEXT UNIQUE NOT NULL, region TEXT NOT NULL, region_code INT NOT NULL, city TEXT NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL)";

    }

    private void addStationsData(SQLiteDatabase db){
        InputStream inStream = context.getResources().openRawResource(R.raw.stations);

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));

        String line;
        db.beginTransaction();
        try {
            while ((line = buffer.readLine()) != null) {
                String[] colums = line.split(";");
                if (colums.length != 7) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                ContentValues cv = new ContentValues(7);
                // # name;id;region;region_code;city;lat;lon
                cv.put(StationEntry.NAME, capitalizeString(colums[0].trim()));
                cv.put(StationEntry.CODE, colums[1].trim());
                cv.put(StationEntry.REGION, colums[2].trim());
                cv.put(StationEntry.REGION_CODE, colums[3].trim());
                cv.put(StationEntry.CITY, colums[4].trim());
                cv.put(StationEntry.LATITUDE, colums[5].trim());
                cv.put(StationEntry.LONGITUDE, colums[6].trim());
                db.insert(StationEntry.TABLE_NAME, null, cv);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private String capitalizeString(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!words[i].equals("")){ // Serve se ci sono due spazi attaccati
                ret.append(Character.toUpperCase(words[i].charAt(0)));
                ret.append(words[i].substring(1).toLowerCase());
                if (i < words.length - 1) {
                    ret.append(' ');
                }
            }
        }
        return ret.toString();
    }
}
