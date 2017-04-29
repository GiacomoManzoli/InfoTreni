package com.manzolik.gmanzoli.mytrains.service;

import android.os.AsyncTask;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
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
import java.util.List;

/**
 * Classe che si occupa di recuperare i dati relativi allo stato (andamento) di un treno
 * */

public class TrainStatusService {

    private static final String TAG = TrainStatusService.class.getSimpleName();


    private Exception error;
    private TrainReminder trainReminder;

    public void getStatusForTrain(final String pTrainCode, final String pDeptCode, final TrainStatusServiceListener listener){

        new AsyncTask<String, Void, String>() {

            private String trainCode, deptCode;

            @Override
            protected String doInBackground(String... args) {
                trainCode = args[0];
                deptCode = args[1];
                String endpoint = String.format("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%s", deptCode, trainCode);
                if (BuildConfig.DEBUG) Log.d(TAG, endpoint);

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
                    listener.onTrainStatusFailure(error);
                    return;
                }

                try {
                    JSONObject data = new JSONObject(s);

                    TrainStatus ts = new TrainStatus();
                    // IMPORTANTE: prima di chiamare il metodo populate è necessario
                    // impstare la stazione target
                    if (TrainStatusService.this.trainReminder != null){
                        ts.setTargetStation(TrainStatusService.this.trainReminder.getTargetStaion());
                    }
                    ts.populate(data);
                    listener.onTrainStatusSuccess(ts);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onTrainStatusFailure(
                            new TrainStatusNotFound("Non è stato possibile recuperare lo stato del treno "+trainCode + " da "+deptCode));
                }

            }
        }.execute(pTrainCode, pDeptCode);
    }

    public void getStatusForTrainReminder(TrainReminder t, final TrainStatusServiceListener listener){
        Train train = t.getTrain();
        /*
        * Salva un riferimento al reminder in modo che getStatusForTrain(String, String) imposti
        * correttamente il campo dati dell'oggetto TrainStatus che viene passato alla callback */
        trainReminder = t;
        this.getStatusForTrain(
                String.format("%s", train.getCode()),
                train.getDepartureStation().getCode(),
                listener);
    }

    public interface TrainStatusServiceListener {
        void onTrainStatusSuccess(TrainStatus ts);
        void onTrainStatusFailure(Exception e);
    }

    public class TrainStatusNotFound extends Exception {
        public TrainStatusNotFound(String detailMessage) {
            super(detailMessage);
        }
    }

}
