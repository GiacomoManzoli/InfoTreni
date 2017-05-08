package com.manzolik.gmanzoli.mytrains.service;

import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/*
 * Classe che si occupa di trovare le fermate che vengono effettuate da un treno
 * */

public class TrainStopsService implements HttpGetTask.HttpGetTaskListener {

    private static final String TAG = TrainStopsService.class.getSimpleName();
    private static final String ENDPOINT_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%s";

    private TrainStopsServiceListener mListener;
    private boolean mQueryInProgress = false;

    public boolean getTrainStops(Train train, TrainStopsServiceListener listener){
        if (mQueryInProgress) {return false;}
        mQueryInProgress = true;
        String endpoint = String.format(ENDPOINT_FORMAT, train.getDepartureStation().getCode(), train.getCode());
        mListener = listener;
        new HttpGetTask(endpoint, this).execute();
        return true;
    }

    @Override
    public void onHttpGetTaskCompleted(String response) {
        mQueryInProgress = false;
        try {
            List<String> stationList = new ArrayList<>();
            JSONObject data = new JSONObject(response);
            JSONArray stopsArray = data.optJSONArray("fermate");
            for (int i = 0; i < stopsArray.length(); i++) {
                JSONObject obj = stopsArray.getJSONObject(i);
                stationList.add(StringUtils.capitalizeString(obj.optString("stazione")));
            }

            if (mListener != null) mListener.onTrainStopsSuccess(stationList);
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Errore nel parsing del JSON");
            e.printStackTrace();

            if (mListener != null) {
                mListener.onTrainStopsFailure(new InvalidTrainStops("Non è stato possibile recuperare le fermate del treno"));
            }
        } finally {
            mListener = null;
        }
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        mQueryInProgress = false;
        if (mListener != null) {
            mListener.onTrainStopsFailure(e);
            mListener = null;
        }
    }

    public interface TrainStopsServiceListener {
        void onTrainStopsSuccess(List<String> stationNameList);
        void onTrainStopsFailure(Exception exc);
    }

    public class InvalidTrainStops extends Exception {
        InvalidTrainStops(String detailMessage) {
            super(detailMessage);
        }
    }
}
