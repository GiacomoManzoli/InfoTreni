package com.manzolik.gmanzoli.mytrains.service;


// http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/soluzioniViaggioNew/5706/2593/2016-02-26T00:00:00

import android.os.AsyncTask;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.TravelSolution;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TravelSolutionsService {

    private Exception error;

    public void findSolutions(Station fromStation, Station toStation, Calendar date,final int limit, final TravelSolutionsServiceCallback callback) {

        String from = fromStation.getCode().substring(1);
        String to = toStation.getCode().substring(1);
        //2016-02-26T00:00:00
        Calendar correctDate = Calendar.getInstance();
        correctDate.set(Calendar.HOUR, date.get(Calendar.HOUR));
        correctDate.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String when =format.format(correctDate.getTime());
        System.err.println(when);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String f = params[0];
                String t = params[1];
                String w = params[2];
                String endpoint = String.format("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/soluzioniViaggioNew/%s/%s/%s",f,t,w);
                System.out.println(endpoint);

                try {
                    URL url = new URL(endpoint);
                    URLConnection connection = url.openConnection();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null){
                        result.append(line).append("\n");
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
                    callback.travelSolutionsServiceCallbackFailure(error);
                    return;
                }

                try {
                    List<TravelSolution> results = new ArrayList<>();

                    if (response.equals("")){
                        // Nessun risultato trovato
                        callback.travelSolutionsServiceCallbackFailure(new Exception("Errore sconosciuto"));
                        return;
                    }

                    JSONObject obj = new JSONObject(response);
                    JSONArray sols = obj.optJSONArray("soluzioni");

                    if (sols == null || sols.length() == 0) {
                        callback.travelSolutionsServiceCallbackFailure(new NoSolutionsFoundException("Non sono strate trovate soluzioni"));
                        return;
                    }
                    for (int i = 0; i < sols.length() && i < limit; i++) {
                        TravelSolution ts = new TravelSolution();
                        ts.populate(sols.getJSONObject(i));
                        results.add(ts);
                    }

                    callback.travelSolutionsServiceCallbackSuccess(results);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.travelSolutionsServiceCallbackFailure(new Exception("Errore sconosciuto"));
                }

            }
        }.execute(from, to, when);
    }

    public class NoSolutionsFoundException extends Exception {
        public NoSolutionsFoundException(String message) {
            super(message);
        }
    }
}
