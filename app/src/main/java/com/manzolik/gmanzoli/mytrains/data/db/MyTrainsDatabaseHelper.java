package com.manzolik.gmanzoli.mytrains.data.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/*
* Approfondimenti sul modo migliore di usare SQLiteOpenHelper
* - http://touchlabblog.tumblr.com/post/24474398246/android-sqlite-locking
* - http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
*
* Performance di getWritableDatabase
* https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html#getWritableDatabase()
*
* Memory leak con context:
* http://stackoverflow.com/questions/8888530/is-it-ok-to-have-one-instance-of-sqliteopenhelper-shared-by-all-activities-in-an
*
* Non serve chiamare close()
* http://stackoverflow.com/questions/6608498/best-place-to-close-database-connection
* --> Bottom line: campo dati statico che tiene l'istanza unica dell'helper che deve essere
* creata utilizzanto il contesto dell'applicazione.
* */

class MyTrainsDatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "my_trains.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = MyTrainsDatabaseHelper.class.getSimpleName();

    // NOTA: deve essere inizializzata con il contesto dell'applicazione per non
    // creare un MemoryLeak
    @SuppressLint("StaticFieldLeak")
    private static MyTrainsDatabaseHelper mInstance;

    private final Context mContext;


    public static synchronized MyTrainsDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyTrainsDatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }


    private MyTrainsDatabaseHelper(Context context) {
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
                ContentValues cv = new ContentValues(8);
                // # name;id;region;region_code;city;lat;lon
                cv.put(StationTable.NAME, StringUtils.capitalizeString(columns[0].trim()));
                cv.put(StationTable.CODE, columns[1].trim());
                cv.put(StationTable.REGION, columns[2].trim());
                cv.put(StationTable.REGION_CODE, columns[3].trim());
                cv.put(StationTable.CITY, columns[4].trim());
                cv.put(StationTable.LATITUDE, columns[5].trim());
                cv.put(StationTable.LONGITUDE, columns[6].trim());
                cv.put(StationTable.MAINTENANCE_REQUIRED, 0); // Flag che segnala la mancanza di dati
                db.insert(StationTable.TABLE_NAME, null, cv);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
