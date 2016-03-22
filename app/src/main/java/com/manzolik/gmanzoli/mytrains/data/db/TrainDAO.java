package com.manzolik.gmanzoli.mytrains.data.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;

public class TrainDAO extends MyTrainsDatabaseHelper{
    private final Context context;
    public TrainDAO(Context context) {
        super(context);
        this.context = context;
    }

    public Train getTrainFromCode(int trainCode){
        SQLiteDatabase db = getReadableDatabase();

        String[] proj = {
                TrainEntry._ID,
                TrainEntry.CODE,
                TrainEntry.DEPARTURE_STATION
        };
        Cursor c = db.query(TrainEntry.TABLE_NAME,proj,TrainEntry.CODE+"=?",new String[]{String.format("%d", trainCode)}, null, null, null);

        if (c.getCount() == 0){
            return null;
        }
        c.moveToFirst();
        int id = c.getInt(c.getColumnIndex(TrainEntry._ID));
        int stationId = c.getInt(c.getColumnIndex(TrainEntry.DEPARTURE_STATION));

        StationDAO stationDAO = new StationDAO(context);
        Station station = stationDAO.getStationFromID(stationId);

        c.close();
        close();
        return new Train(id,trainCode,station);
    }


    public Train getTrainFromID(int trainId){
        SQLiteDatabase db = getReadableDatabase();

        String[] proj = {
                TrainEntry._ID,
                TrainEntry.CODE,
                TrainEntry.DEPARTURE_STATION
        };
        Cursor c = db.query(TrainEntry.TABLE_NAME,proj,TrainEntry._ID+"=?",new String[]{String.format("%d", trainId)}, null, null, null);

        if (c.getCount() == 0){
            return null;
        }
        c.moveToFirst();
        int trainCode = c.getInt(c.getColumnIndex(TrainEntry.CODE));
        int stationId = c.getInt(c.getColumnIndex(TrainEntry.DEPARTURE_STATION));

        StationDAO stationDAO = new StationDAO(context);
        Station station = stationDAO.getStationFromID(stationId);

        c.close();
        close();
        return new Train(trainId,trainCode,station);
    }

    /**
     * Se il treno non è presente nel database lo inserisce e ritorna l'id del treno
     * altrimenti ritorna l'id
     * */
    public int insertTrainIfNotExists(int code, int departureID) {
        Train t = getTrainFromCode(code);
        if (t != null){
            return t.getID();
        }
        // Inserimento del treno nel database
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrainEntry.CODE, code);
        values.put(TrainEntry.DEPARTURE_STATION, departureID);

        //TODO: potrebbe essere necessario controllare che la stazione sia effettivamente presente

        long newRowId = db.insert(TrainEntry.TABLE_NAME,null, values);
        close();
        return (int)newRowId;
    }


}
