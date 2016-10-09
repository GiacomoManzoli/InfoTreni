package com.manzolik.gmanzoli.mytrains.service;



import android.os.AsyncTask;

import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class TrainDepartureStationService {

    private Exception error;
    private final StationDAO stationDao;

    public TrainDepartureStationService(StationDAO stationDao) {
        this.stationDao = stationDao;
    }

    public void getDepartureStations(final int trainCode, final TrainDepartureStationServiceListener listener){

        System.out.println("GETTING DEPARTURE STATION");
        new AsyncTask<Integer, Void, String>() {

            @Override
            protected String doInBackground(Integer... tr) {
                int trainCode = tr[0];
                String endpoint = String.format("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/cercaNumeroTrenoTrenoAutocomplete/%d",trainCode);
                System.out.println(endpoint);

                try {
                    URL url = new URL(endpoint);
                    URLConnection connection = url.openConnection();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null){
                        result.append(line+"\n");
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
                    listener.onTrainDepartureStationFailure(error);
                    return;
                }

                try {
                    List<Station> stationList = new ArrayList<>();
                    String[] rows = response.split("\n"); //Se ci sono più stazioni possibili i risultati sono su più righe

                    if (response.equals("") || rows[0].equals("")){
                        // Nessun risultato trovato
                        listener.onTrainDepartureStationFailure(new TrainNotFoundException(String.format("Non è stato trovato un treno con codice %d", trainCode)));
                        return;
                    }

                    for (String row: rows) {
                        // 2233 - VENEZIA S. LUCIA|2233-S02593
                        String[] codes = row.split("-");
                        Station s = stationDao.getStationFromCode(codes[2]);
                        if (s != null) {
                            stationList.add(s);
                        }
                    }
                    listener.onTrainDepartureStationSuccess(stationList);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onTrainDepartureStationFailure(new Exception("Something went wrong"));
                }

            }
        }.execute(trainCode);
    }

    public interface TrainDepartureStationServiceListener {
        void onTrainDepartureStationSuccess(List<Station> trains);
        void onTrainDepartureStationFailure(Exception exc);
    }

    public class TrainNotFoundException extends Exception {
        public TrainNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}
