package com.manzolik.gmanzoli.mytrains.service;

import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Classe che si occupa di recuperare i dati relativi allo stato (andamento) di un treno
 * */

public class TrainStatusService implements HttpGetTask.HttpGetTaskListener {

    private static final String TAG = TrainStatusService.class.getSimpleName();
    private static final String ENDPOINT_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%s";

    private TrainReminder mTrainReminder;

    private TrainStatusServiceListener mListener;

    /* Recupera lo stato di un treno */
    public void getStatusForTrain(Train train, TrainStatusServiceListener listener){
        mListener = listener;
        String endpoint = String.format(ENDPOINT_FORMAT, train.getDepartureStation().getCode(), train.getCode());

        new HttpGetTask(endpoint, this).execute();
    }

    /* Recupera lo stato di un reminder */
    public void getStatusForTrainReminder(TrainReminder t, TrainStatusServiceListener listener){
        Train train = t.getTrain();
        /* Salva un riferimento al reminder in modo che getStatusForTrain(Train) imposti
         * correttamente il campo dati dell'oggetto TrainStatus che viene passato alla callback */
        mTrainReminder = t;
        this.getStatusForTrain(train, listener);
    }

    @Override
    public void onHttpGetTaskCompleted(String response) {
        try {
            JSONObject data = new JSONObject(response);

            TrainStatus ts = new TrainStatus();
            // IMPORTANTE: prima di chiamare il metodo populate è necessario
            // impstare la stazione target
            if (mTrainReminder != null){
                ts.setTargetStation(mTrainReminder.getTargetStation());
            }
            ts.populate(data);
            if (mListener != null) {
                mListener.onTrainStatusSuccess(ts);
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Errore nel parsing del JSON");
            e.printStackTrace();
            if (mListener != null) {
                mListener.onTrainStatusFailure(new InvalidTrainStatus("Non è stato possibile recuperare lo stato del treno"));
            }
        } finally {
            // Query completata, cancello il riferimento al listener
            mListener = null;
        }
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        if (mListener != null) {
            mListener.onTrainStatusFailure(e);
            mListener = null;
        }
    }

    /*
    * Listener
    * */
    public interface TrainStatusServiceListener {
        void onTrainStatusSuccess(TrainStatus ts);
        void onTrainStatusFailure(Exception e);
    }

    /*
     * Eccezione sollevabile dal servizio
     * */
    public class InvalidTrainStatus extends Exception {
        InvalidTrainStatus(String detailMessage) {
            super(detailMessage);
        }
    }

}
