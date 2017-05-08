package com.manzolik.gmanzoli.mytrains.service;

import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.StationArrival;
import com.manzolik.gmanzoli.mytrains.data.StationDeparture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

// http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/partenze/S02581/Mon%20May%2008%202017%2017:19:34%20GMT+0200%20(CEST)
// http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/arrivi/S02581/Mon%20May%2008%202017%2017:19:34%20GMT+0200%20(CEST)

// Mon May 08 2017 17:19:34 GMT+0200 (CEST)
// "EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(CEST)"

public class StationStatusService implements HttpGetTask.HttpGetTaskListener {
    private static final String ENDPOINT_ARRIVAL_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/arrivi/%s/%s";
    private static final String ENDPOINT_DEPARTURE_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/partenze/%s/%s";

    // Identificatori delle query in esecuzione
    private static final int QUERY_NONE = -1;
    private static final int QUERY_ARRIVAL = 0;
    private static final int QUERY_DEPARTURE = 1;

    // Il sito viaggiatreno aggiunge anche il "(CEST)" ma la richiesta va a buon fine anche senza
    //private static final String DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(CEST)'";
    private static final String DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    private static final String TAG = StationStatusService.class.getSimpleName();

    private StationStatusArrivalsListener mArrivalListener;
    private StationStatusDeparturesListener mDepartureListener;
    private SimpleDateFormat mDateFormat;
    private int mCurrentQuery = QUERY_NONE;


    public StationStatusService() {
        // La data deve essere in formato US.
        // Lo so, non ha senso, ma sotto sotto anche Trenitalia non ha senso.
        mDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    }


    public boolean getStationArrivals(Station s, StationStatusArrivalsListener listener) {
        if (mCurrentQuery == QUERY_NONE) {
            mCurrentQuery = QUERY_ARRIVAL;
            mArrivalListener = listener;
            String stationCode = s.getCode();
            String date = mDateFormat.format(Calendar.getInstance().getTime());
            String endpoint = String.format(Locale.getDefault(), ENDPOINT_ARRIVAL_FORMAT, stationCode, date);
            new HttpGetTask(endpoint, this).execute();
            return true;
        } else {
            return false;
        }

    }

    public boolean getStationDepartures(Station s, StationStatusDeparturesListener listener) {
        if (mCurrentQuery == QUERY_NONE) {
            mCurrentQuery = QUERY_DEPARTURE;
            mDepartureListener = listener;
            String stationCode = s.getCode();
            String date = mDateFormat.format(Calendar.getInstance().getTime());
            String endpoint = String.format(Locale.getDefault(), ENDPOINT_DEPARTURE_FORMAT, stationCode, date);
            new HttpGetTask(endpoint, this).execute();
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onHttpGetTaskCompleted(String response) {
        if (BuildConfig.DEBUG) Log.d(TAG, "HttpTaskCompleted");
        switch (mCurrentQuery) {
            case QUERY_ARRIVAL:
                if (mArrivalListener != null) {
                    try {
                        List<StationArrival> results = new ArrayList<>();
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            StationArrival arrival = new StationArrival();
                            arrival.populate(array.optJSONObject(i));
                            results.add(arrival);
                        }
                        mArrivalListener.onStationStatusArrivals(results);
                    } catch (JSONException exc) {
                        mArrivalListener.onStationStatusFailure(new InvalidStationStatus());
                    }
                }
                break;
            case QUERY_DEPARTURE:
                if (mDepartureListener != null) {
                    try {
                        List<StationDeparture> results = new ArrayList<>();
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            StationDeparture departure = new StationDeparture();
                            departure.populate(array.optJSONObject(i));
                            results.add(departure);
                        }
                        mDepartureListener.onStationStatusDepartures(results);
                    } catch (JSONException exc) {
                        mDepartureListener.onStationStatusFailure(new InvalidStationStatus());
                    }
                }
                break;
        }
        mCurrentQuery = QUERY_NONE;
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        if (BuildConfig.DEBUG) Log.e(TAG, "Errore! "+ e.toString());
        if (mCurrentQuery == QUERY_ARRIVAL && mArrivalListener != null) {
            mArrivalListener.onStationStatusFailure(e);
        } else if (mCurrentQuery == QUERY_DEPARTURE && mDepartureListener != null) {
            mDepartureListener.onStationStatusFailure(e);
        }
        mCurrentQuery = QUERY_NONE;
    }


    /*
    * Listener
    * */
    public interface StationStatusArrivalsListener {
        void onStationStatusArrivals(List<StationArrival> arrivals);
        void onStationStatusFailure(Exception exc);
    }

    public interface StationStatusDeparturesListener {
        void onStationStatusDepartures(List<StationDeparture> departures);
        void onStationStatusFailure(Exception exc);
    }

    public class InvalidStationStatus extends Exception {
        InvalidStationStatus() {
            super("Errore nel recuperare lo stato della stazione");
        }
    }
}
