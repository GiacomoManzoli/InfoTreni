package com.manzolik.gmanzoli.mytrains.http;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import java.util.ArrayList;
import java.util.List;

public class TrainDepartureStationService implements HttpGetTask.HttpGetTaskListener{
    private static final String ENDPOINT_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/cercaNumeroTrenoTrenoAutocomplete/%s";

    private final StationDAO mStationDao;
    private TrainDepartureStationServiceListener mListener;

    private String mLastQueryTrainCode;
    private boolean mQueryInProgress = false;


    public TrainDepartureStationService(StationDAO stationDao) {
        this.mStationDao = stationDao;
    }

    public boolean getDepartureStations(final String trainCode, final TrainDepartureStationServiceListener listener){
        if (!mQueryInProgress) {
            mListener = listener;
            String endpoint = String.format(ENDPOINT_FORMAT, trainCode);
            mLastQueryTrainCode = trainCode;
            new HttpGetTask(endpoint, this).execute();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onHttpGetTaskCompleted(String response) {
        mQueryInProgress = false;
        List<Station> stationList = new ArrayList<>();
        String[] rows = response.split("\n"); //Se ci sono più stazioni possibili i risultati sono su più righe

        if (response.equals("") || rows[0].equals("")){
            // Nessun risultato trovato
            if (mListener != null) {
                String msg = String.format("Non è stato trovato un treno con codice %s", mLastQueryTrainCode);
                mListener.onTrainDepartureStationFailure(new TrainNotFoundException(msg));
            }
        } else {
            for (String row: rows) {
                // `row` è una stringa del tipo
                // 2233 - VENEZIA S. LUCIA|2233-S02593
                String[] codes = row.split("-");
                Station s = mStationDao.getStationFromCode(codes[2]);
                if (s != null) {
                    stationList.add(s);
                }
            }
            if (mListener != null) mListener.onTrainDepartureStationSuccess(stationList);
        }
        // Query completata, cancello il riferimento al listener
        mListener = null;
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        mQueryInProgress = false;
        if (mListener != null) {
            mListener.onTrainDepartureStationFailure(e);
            mListener = null;
        }
    }

    /*
     * Listener
     * */
    public interface TrainDepartureStationServiceListener {
        void onTrainDepartureStationSuccess(List<Station> trains);
        void onTrainDepartureStationFailure(Exception exc);
    }

    /*
     * Eccezione sollevabile dal servizio
     * */
    public class TrainNotFoundException extends Exception {
        TrainNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}
