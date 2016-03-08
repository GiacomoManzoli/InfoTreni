package com.manzolik.gmanzoli.mytrains.service;

import android.net.Uri;
import android.os.AsyncTask;

import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by gmanzoli on 26/02/16.
 */
public class TrenitaliaService implements TrenitaliaServiceCallback{

    private Exception error;
    private List<TrainStatus> trainStatusList; //lista con i risultati parziali
    private boolean queryInProgress = false;
    private int callbackCount;
    private TrenitaliaServiceCallback resultCallback;

    public void getTrainStatusList(TrenitaliaServiceCallback callback, List<TrainReminder> reminderList){
        if (queryInProgress) {
            callback.serviceFailure(new QueryInProgressException("C'è già una query in esecuzione"));
            return;
        }
        queryInProgress = true;
        resultCallback = callback;

        List<TrainReminder> trainList = new ArrayList<>(); //Deve contenere i treni da richiedere
        Calendar currentTime = Calendar.getInstance();
        for(TrainReminder tr: reminderList){
            if (tr.shouldShowReminder(currentTime)) {
                trainList.add(tr);
            }
        }

        trainStatusList = new ArrayList<>();
        if (trainList.size() == 0){
            queryInProgress = false;
            callback.serviceSuccess(trainStatusList);
        } else{
            callbackCount = trainList.size();
            for (TrainReminder t: trainList) {
                getStatusForTrain(t, this);
            }
        }


    }

    private void getStatusForTrain(TrainReminder t, final TrenitaliaServiceCallback callback){
        // Esegue una chiamata
        new AsyncTask<TrainReminder, Void, String>() {
            private TrainReminder trainReminder;
            @Override
            protected String doInBackground(TrainReminder... tr) {
                Train train = tr[0].getTrain();
                this.trainReminder = tr[0];

                String endpoint = String.format("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%d", train.getDepartureStation().getCode(), train.getCode());
                System.out.println(endpoint);

                try {
                    URL url = new URL(endpoint);
                    URLConnection connection = url.openConnection();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null){
                        result.append(line);
                    }

                    return result.toString();
                }catch (Exception e){
                    error = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (s == null && error != null){
                    callback.serviceFailure(error);
                    return;
                }

                try {
                    JSONObject data = new JSONObject(s);

                    TrainStatus ts = new TrainStatus(trainReminder);
                    ts.populate(data);


                    List<TrainStatus> tss = new ArrayList<>();
                    tss.add(ts);
                    callback.serviceSuccess(tss);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.serviceFailure(new TrainStatusNotFound("Train status not found"));
                }

            }
        }.execute(t);
    }

    @Override
    public void serviceSuccess(List<TrainStatus> trainStatuses) {
        // Funzione che viene invocata quando sono stati ottenuti i risultati per un treno

        // la lista di treni ha un solo elemento
        trainStatusList.add(trainStatuses.get(0));
        callbackCount--;
        if (callbackCount == 0){
            //Ho tutti i risultati
            queryInProgress = false;
            resultCallback.serviceSuccess(trainStatusList);
        }
    }

    @Override
    public void serviceFailure(Exception exc) {
        // Funzione che viene invocata se non è stato possibile ottenere il risultato per un treno
        queryInProgress = false;
        resultCallback.serviceFailure(exc);
    }


    public class QueryInProgressException extends Exception{
        public QueryInProgressException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class TrainStatusNotFound extends Exception {
        public TrainStatusNotFound(String detailMessage) {
            super(detailMessage);
        }
    }
}
