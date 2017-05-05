package com.manzolik.gmanzoli.mytrains.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


class MyTrainsDatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "my_trains.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = MyTrainsDatabaseHelper.class.getSimpleName();

    private final Context mContext;

    MyTrainsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Creo tabella: " + StationTable.TABLE_NAME);
        db.execSQL(StationTable.CREATE_SQL);
        if (BuildConfig.DEBUG) Log.i(TAG, "Aggiusto stazioni a " + StationTable.TABLE_NAME);
        addStationsData(db);

        if (BuildConfig.DEBUG) Log.i(TAG, "Creo tabella: " + TrainTable.TABLE_NAME);
        db.execSQL(TrainTable.CREATE_SQL);

        if (BuildConfig.DEBUG) Log.i(TAG, "Creo tabella: " + TrainReminderTable.TABLE_NAME);
        db.execSQL(TrainReminderTable.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    private void addStationsData(SQLiteDatabase db){
        InputStream inStream = mContext.getResources().openRawResource(R.raw.stations);

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));

        String line;
        db.beginTransaction();
        try {
            while ((line = buffer.readLine()) != null) {
                String[] columns = line.split(";");
                if (columns.length != 7) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                ContentValues cv = new ContentValues(7);
                // # name;id;region;region_code;city;lat;lon
                cv.put(StationTable.NAME, StringUtils.capitalizeString(columns[0].trim()));
                cv.put(StationTable.CODE, columns[1].trim());
                cv.put(StationTable.REGION, columns[2].trim());
                cv.put(StationTable.REGION_CODE, columns[3].trim());
                cv.put(StationTable.CITY, columns[4].trim());
                cv.put(StationTable.LATITUDE, columns[5].trim());
                cv.put(StationTable.LONGITUDE, columns[6].trim());
                db.insert(StationTable.TABLE_NAME, null, cv);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
