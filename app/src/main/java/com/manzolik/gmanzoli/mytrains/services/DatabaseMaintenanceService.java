package com.manzolik.gmanzoli.mytrains.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.StationArrival;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMaintenanceService extends IntentService {
    private static final String TAG = DatabaseMaintenanceService.class.getSimpleName();
    private static final String GOOGLE_API_URL_FORMAT = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";

    public static final String ARG_SILENT = "arg_silent";

    private NotificationManager mNotificationManager;



    public DatabaseMaintenanceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Eseguito in background, posso fare le richieste HTTP in modo sincrono.
        StationDAO stationDAO = new StationDAO(getApplicationContext());
        List<Station> stationList = stationDAO.getAllStationsWhichNeedsMaintenace();

        boolean silenced = (intent != null) && intent.getBooleanExtra(ARG_SILENT, false);

        if (stationList.size() > 0) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (BuildConfig.DEBUG) Log.d(TAG, "Inizio manutenzione");
            showNotification("Manutenzione del database in corso", true);

            if (BuildConfig.DEBUG) Log.d(TAG, String.format("Stazioni che necessitano di manutenzione: %s", stationList.size()));

            for (Station station : stationList) {
                Station betterStation = askGoogleMapsAPI(station);
                if (betterStation != station) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Migliorata stazione: " + betterStation.getName() + " "+ betterStation.getCity());
                    stationDAO.updateStation(betterStation);
                } else {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Nessun miglioramento: " + betterStation.getName());
                }
            }
            mNotificationManager.cancelAll();
            if (!silenced) {
                showNotification("Manutenzione del database completata", false);
            }
        }

    }

    private void showNotification(String message, boolean persistent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_alt)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setGroup("maintenace");


        Notification notification = builder.build();
        if (persistent) {
            notification.flags = notification.flags
                    | Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        }
        mNotificationManager.notify(0, notification);
    }

    @NonNull
    private Station askGoogleMapsAPI(Station station) {
        String query = station.getName() + " stazione treni";
        query = query.replace(" ","+");
        String API_KEY = getApplicationContext().getResources().getString(R.string.google_maps_api_key);
        String endpoint = String.format(GOOGLE_API_URL_FORMAT, query,API_KEY);
        if (BuildConfig.DEBUG) Log.d(TAG, endpoint);
        String response = getUrl(endpoint);

        try {
            JSONObject data = new JSONObject(response);
            if (data.getString("status").equals("OK")) {
                JSONObject result = data.getJSONArray("results").getJSONObject(0);

                JSONArray types = result.getJSONArray("types");
                List<String> resultType = new ArrayList<>();
                for(int i = 0; i < types.length(); i++){
                    resultType.add(types.getString(i));
                }
                if (resultType.contains("train_station") || resultType.contains("transit_station") || resultType.contains("establishment") || resultType.contains("point_of_interest")) {
                    if (BuildConfig.DEBUG)Log.d(TAG, "Bingo!");
                    // Bingo, ho trovato la stazione su Google!
                    double latitude = result.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double longitude = result.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    JSONArray addressComponents = result.getJSONArray("address_components");

                    if (addressComponents.length() > 0) {
                        String city = getAddressResultComponentFromType(addressComponents, "locality"); // Città
                        String regionName = getAddressResultComponentFromType(addressComponents, "administrative_area_level_1"); // Regione
                        int regionCode = getRegionCodeFromRegionName(regionName);
                        return new Station(station.getId(), station.getName(), station.getCode(), regionName, regionCode, city, latitude, longitude, station.isFavorite());
                    }


                    return new Station(station.getId(), station.getName(), station.getCode(), null, -1, null, latitude, longitude, station.isFavorite());
                } else {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Acqua! riprovo con un'altra query");
                    String[] nameComponents = station.getName().split(" ");
                    String newName = "";
                    for(String comp: nameComponents) {
                        if (comp.length() == 1) continue;
                        newName += comp + " ";
                    }
                    newName = newName.trim();
                    Station otherTry = new Station(newName, station.getName());
                    // Ho trovato qualocosa che non è la stazione
                    Station secondResult = askGoogleMapsAPI(otherTry);
                    if (secondResult == otherTry) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Anche il secondo tentativo è andato male");
                        return station;
                    } else {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Utilizzo la nuova stazione");
                        return new Station(station.getId(), station.getName(), station.getCode(),
                                secondResult.getRegion(), secondResult.getRegionCode(),
                                secondResult.getCity(), secondResult.getLatitude(),
                                secondResult.getLongitude(), station.isFavorite());
                    }
                }
            } else {
                if (BuildConfig.DEBUG) Log.e(TAG, "Google ha risposto: "+ data.getString("status"));
                return station;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            Log.e(TAG, "Cose!!");

            e.printStackTrace();
            return station;
        }
    }

    private String getAddressResultComponentFromType(JSONArray addressResult, String type) throws JSONException {
        for(int i = 0; i < addressResult.length(); i++) {
            JSONObject object = addressResult.getJSONObject(i);
            JSONArray types = object.getJSONArray("types");
            for(int j = 0; j < types.length(); j++){
                if (types.getString(j).equals(type)) {
                    return object.getString("long_name");
                }
            }
        }
        return null;
    }

    private static int getRegionCodeFromRegionName(String regionName) {
        switch (regionName) {
            case "Lombardia": return 1;
            case "Liguria": return 2;
            case "Piemonte": return 3;
            case "Valle d'Aosta": return 4;
            case "Lazio": return 5;
            case "Umbria": return 6;
            case "Molise": return 7;
            case "Emilia Romagna": return 8;
            case "Trentino Alto Adige": return 9;
            case "Friuli Venezia Giulia": return 10;
            case "Marche": return 11;
            case "Veneto": return 12;
            case "Toscana": return 13;
            case "Sicilia": return 14;
            case "Basilicata": return 15;
            case "Puglia": return 16;
            case "Calabria": return 17;
            case "Campania": return 18;
            case "Abruzzo": return 19;
            case "Sardegna": return 20;
            default: return 0;
        }
    }

    private String getUrl(String endpoint) {
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

        } catch (IOException e){
            return "";
        }
    }


}
