package com.manzolik.gmanzoli.mytrains.data.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TrainDepartureStationService implements HttpGetTask.HttpGetTaskListener{
    private static final String ENDPOINT_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/cercaNumeroTrenoTrenoAutocomplete/%s";
    private static final String TAG = TrainDepartureStationService.class.getSimpleName();

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
            List<String> newRows = new ArrayList<>();
            for (String row: rows) {
                // `row` è una stringa del tipo
                // 2233 - VENEZIA S. LUCIA|2233-S02593
                String[] codes = row.split("-");
                Station s = mStationDao.getStationFromCode(codes[2]);
                if (s != null) {
                    stationList.add(s);
                } else {
                    // La stazione di partenza non è nel database interno
                    newRows.add(row);
                }
            }
            if (newRows.size() > 0) {
                // Ci sono delle stazioni nuove da aggiungere al database
                if (BuildConfig.DEBUG) Log.e(TAG, "IL DATABASE INTERNO NON E' SINCRONIZZATO CON QUELLO DI VIAGGIATRENO");
                String msg = "Il database dell'applicazione necessita di un aggiornamento";
                /*
                * Crea degli oggetti "dummy" per le nuove stazioni per passare i dati
                * al listener il quale si occuperà di aggiungerle al database.
                * */
                List<Station> newStations = new ArrayList<>();
                for (String row: newRows) {
                    // `row` è una stringa del tipo
                    // 2233 - VENEZIA S. LUCIA|2233-S02593
                    String newStationCode = row.split("-")[2];
                    String newTrainCode = row.split("-")[0].trim();
                    /*
                    Stranamente così non funziona
                    String trainFullDescription = row.split("|")[0];
                    String newStationName = trainFullDescription.split("-")[1].trim();*/
                    String newStationName = StringUtils.capitalizeString(row.replace(newTrainCode, "")
                            .replace(newStationCode, "")
                            .replace("-", "")
                            .replace("|", "")
                            .trim());

                    if (BuildConfig.DEBUG) Log.d(TAG, "Stazione sconosciuta: "
                            + newStationCode + " - " + newStationName);

                    newStations.add(new Station(newStationName, newStationCode));

                }

                if (mListener != null) mListener.onTrainDepartureStationFailure(new DatabaseNeedsUpdate(msg, stationList, newStations));
            } else {
                if (mListener != null) mListener.onTrainDepartureStationSuccess(stationList);
            }
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
    public class DatabaseNeedsUpdate extends Exception{
        private List<Station> mPartials;
        private List<Station> mNewStations;

        DatabaseNeedsUpdate(String message, List<Station> partials, List<Station> newStations) {
            super(message);
            mPartials = partials;
            mNewStations = newStations;
        }

        @NonNull
        public List<Station> getPartials() {
            return mPartials;
        }

        @NonNull
        public List<Station> getNewStations() {
            return mNewStations;
        }
    }
}
