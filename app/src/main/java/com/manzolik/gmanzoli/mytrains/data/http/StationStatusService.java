package com.manzolik.gmanzoli.mytrains.data.http;

import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.StationArrival;
import com.manzolik.gmanzoli.mytrains.data.StationDeparture;
import com.manzolik.gmanzoli.mytrains.data.StationInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/partenze/S02581/Mon%20May%2008%202017%2017:19:34%20GMT+0200%20(CEST)
// http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/arrivi/S02581/Mon%20May%2008%202017%2017:19:34%20GMT+0200%20(CEST)

// Mon May 08 2017 17:19:34 GMT+0200 (CEST)
// "EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(CEST)"

public class StationStatusService implements HttpGetTask.HttpGetTaskListener {
    private static final String ENDPOINT_ARRIVAL_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/arrivi/%s/%s";
    private static final String ENDPOINT_DEPARTURE_FORMAT = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/partenze/%s/%s";

    // Identificatori delle query
    public enum StatusInfoQueryType implements Serializable {
        QUERY_NONE, QUERY_ARRIVAL, QUERY_DEPARTURE
    }
    // Il sito viaggiatreno aggiunge anche il "(CEST)" ma la richiesta va a buon fine anche senza
    //private static final String DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(CEST)'";
    private static final String DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    private static final String TAG = StationStatusService.class.getSimpleName();

    private StationStatusListener mListener;
    private SimpleDateFormat mDateFormat;
    private StatusInfoQueryType mCurrentQuery = StatusInfoQueryType.QUERY_NONE;


    public StationStatusService() {
        // La data deve essere in formato US.
        // Lo so, non ha senso, ma sotto sotto anche Trenitalia non ha senso.
        mDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    }


    public boolean getStationInfos(Station s, StatusInfoQueryType queryType, StationStatusListener listener) {
        if (queryType == StatusInfoQueryType.QUERY_NONE) { return false; }

        if (mCurrentQuery == StatusInfoQueryType.QUERY_NONE) {
            mCurrentQuery = queryType;
            mListener = listener;
            String stationCode = s.getCode();
            String date = mDateFormat.format(Calendar.getInstance().getTime());

            String baseEndpoint =
                    (queryType == StatusInfoQueryType.QUERY_ARRIVAL)?
                        ENDPOINT_ARRIVAL_FORMAT :
                        ENDPOINT_DEPARTURE_FORMAT;

            String endpoint = String.format(Locale.getDefault(), baseEndpoint, stationCode, date);
            new HttpGetTask(endpoint, this).execute();
            return true;
        } else {
            return false;
        }

    }


    @Override
    public void onHttpGetTaskCompleted(String response) {
        if (BuildConfig.DEBUG) Log.d(TAG, "HttpTaskCompleted");

        if (mListener != null) {
            try {
                List<StationInfo> results = new ArrayList<>();
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    StationInfo stationInfo;
                    switch (mCurrentQuery) {
                        case QUERY_ARRIVAL:
                            stationInfo = new StationArrival();
                            break;
                        case QUERY_DEPARTURE:
                        default:
                            stationInfo = new StationDeparture();
                            break;
                    }

                    stationInfo.populate(array.optJSONObject(i));
                    results.add(stationInfo);
                }
                mListener.onStationStatusResult(results, mCurrentQuery);
            } catch (JSONException exc) {
                mListener.onStationStatusFailure(new InvalidStationStatus());
            }
        }
        mCurrentQuery = StatusInfoQueryType.QUERY_NONE;
    }

    @Override
    public void onHttpGetTaskFailed(Exception e) {
        if (BuildConfig.DEBUG) Log.e(TAG, "Errore! "+ e.toString());
        if (mCurrentQuery == StatusInfoQueryType.QUERY_ARRIVAL && mListener != null) {
            mListener.onStationStatusFailure(e);
        } else if (mCurrentQuery == StatusInfoQueryType.QUERY_DEPARTURE && mListener != null) {
            mListener.onStationStatusFailure(e);
        }
        mCurrentQuery = StatusInfoQueryType.QUERY_NONE;
    }


    /*
    * Listener
    * */
    public interface StationStatusListener {
        void onStationStatusResult(List<StationInfo> infos, StatusInfoQueryType queryType);
        void onStationStatusFailure(Exception exc);
    }



    public class InvalidStationStatus extends Exception {
        InvalidStationStatus() {
            super("Errore nel recuperare lo stato della stazione");
        }
    }
}
