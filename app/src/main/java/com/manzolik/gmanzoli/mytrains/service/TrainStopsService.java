package com.manzolik.gmanzoli.mytrains.service;

import android.os.AsyncTask;

import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Classe che si occupa di trovare le fermete che vengono effettuate da un treno
 * */

public class TrainStopsService {

    private Exception error;



    public void getTrainStops(final String trainCode, final String departureStationCode, final TrainStopsServiceListener listener){
        System.out.println("GETTING TRAIN STOPS");
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... tr) {
                String endpoint = String.format("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%s", departureStationCode, trainCode);
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
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                if (response == null && error != null){
                    listener.onTrainStopsFailure(error);
                    return;
                }

                try {
                    List<String> stationList = new ArrayList<>();
                    JSONObject data = new JSONObject(response);
                    JSONArray stopsArray = data.optJSONArray("fermate");
                    for (int i = 0; i < stopsArray.length(); i++) {
                        JSONObject obj = stopsArray.getJSONObject(i);
                        System.out.println(obj);
                        stationList.add(StringUtils.capitalizeString(obj.optString("stazione")));
                    }

                    listener.onTrainStopsSuccess(stationList);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onTrainStopsFailure(new Exception("TrainStopsService: Something went wrong"));
                }

            }
        }.execute(trainCode);
    }


    public interface TrainStopsServiceListener {
        void onTrainStopsSuccess(List<String> stationNameList);
        void onTrainStopsFailure(Exception exc);
    }
}
