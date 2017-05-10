package com.manzolik.gmanzoli.mytrains.data.http;

// http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/soluzioniViaggioNew/5706/2593/2016-02-26T00:00:00
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.TravelSolution;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/*
 * Classe che si occupa di recuperare le possibili soluzioni per un viaggio tra due stazioni
 * */

public class TravelSolutionsService implements HttpGetTask.HttpGetTaskListener {

    private static final String TAG = TravelSolutionsService.class.getSimpleName();
    private static final String ENDPOINT_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/soluzioniViaggioNew/%s/%s/%s";

    private TravelSolutionsServiceListener mListener;
    private int mLastQueryLimit;
    private boolean mQueryInProgress = false;

    public boolean findSolutions(Station fromStation, Station toStation, Calendar date, int limit, TravelSolutionsServiceListener listener) {
        if (mQueryInProgress) { return false; }
        mQueryInProgress = true;
        mListener = listener;
        mLastQueryLimit = limit;
        // devo togliere la S
        String from = fromStation.getCode().substring(1);
        String to = toStation.getCode().substring(1);
        //2016-02-26T00:00:00

        Calendar correctDate = Calendar.getInstance();
        correctDate.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
        correctDate.set(Calendar.MINUTE, date.get(Calendar.MINUTE));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        String when = format.format(correctDate.getTime());
        String endpoint = String.format(ENDPOINT_FORMAT, from, to, when);

        new HttpGetTask(endpoint, this).execute();
        return true;
    }


    @Override
    public void onHttpGetTaskCompleted(String response) {
        mQueryInProgress = false;
        try {
            List<TravelSolution> results = new ArrayList<>();

            JSONObject obj = new JSONObject(response);
            JSONArray sols = obj.optJSONArray("soluzioni");
            String departureName = obj.optString("origine");
            String arrivalName = obj.optString("destinazione");

            if (response.equals("") || sols == null || sols.length() == 0) {
                if (mListener != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Non sono state trovate soluzioni");
                    String msg = "Non sono state trovate soluzioni per la tratta.";
                    mListener.onTravelSolutionsFailure(new NoSolutionsFoundException(msg));
                }
            } else {
                if (mLastQueryLimit <= 0 || sols.length() < mLastQueryLimit) {
                    mLastQueryLimit = sols.length();
                }
                for (int i = 0; i < mLastQueryLimit; i++) {
                    TravelSolution ts = new TravelSolution();
                    ts.populate(sols.getJSONObject(i));
                    ts.setDepartureStationName(departureName);
                    ts.setArrivalStaionName(arrivalName);
                    results.add(ts);
                }
                if (mListener != null) mListener.onTravelSolutionsSuccess(results);
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Errore nel parsing del JSON");
            e.printStackTrace();

            if (mListener != null) {
                String msg = "Errore nell'eleborazione delle soluzioni di viaggio";
                mListener.onTravelSolutionsFailure(new InvalidTravelSolutions(msg));
            }
        }
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        mQueryInProgress = false;
        if (mListener != null) {
            mListener.onTravelSolutionsFailure(e);
            mListener = null;
        }
    }


    public interface TravelSolutionsServiceListener {
        void onTravelSolutionsSuccess(List<TravelSolution> solutions);
        void onTravelSolutionsFailure(Exception exc);
    }


    public class NoSolutionsFoundException extends Exception {
        NoSolutionsFoundException(String message) {
            super(message);
        }
    }

    public class InvalidTravelSolutions extends Exception {
        InvalidTravelSolutions(String message) {
            super(message);
        }
    }
}
