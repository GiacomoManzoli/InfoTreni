package com.manzolik.gmanzoli.mytrains.service;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class TrainStopsService {

    private Exception error;



    public void getTrainStops(final int trainCode, final String departureStationCode, final TrainStopsServiceCallback callback){
        System.out.println("GETTING TRAIN STOPS");
        new AsyncTask<Integer, Void, String>() {

            @Override
            protected String doInBackground(Integer... tr) {
                String endpoint = String.format("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/andamentoTreno/%s/%d", departureStationCode, trainCode);
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
                    callback.trainStopsServiceCallbackFailure(error);
                    return;
                }

                try {
                    List<String> stationList = new ArrayList<>();
                    JSONObject data = new JSONObject(response);
                    JSONArray stopsArray = data.optJSONArray("fermate");
                    for (int i = 0; i < stopsArray.length(); i++) {
                        JSONObject obj = stopsArray.getJSONObject(i);
                        System.out.println(obj);
                        stationList.add(capitalizeString(obj.optString("stazione")));
                    }

                    callback.trainStopsServiceCallbackSuccess(stationList);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.trainStopsServiceCallbackFailure(new Exception("Something went wrong"));
                }

            }
        }.execute(trainCode);
    }

    private String capitalizeString(String toBeCapped){
        String[] tokens = toBeCapped.split("\\s");
        String result = "";

        for(int i = 0; i < tokens.length; i++){
            char capLetter = Character.toUpperCase(tokens[i].charAt(0));
            result +=  " " + capLetter + tokens[i].substring(1).toLowerCase();
        }
        result = result.trim();
        return result;
    }
}
