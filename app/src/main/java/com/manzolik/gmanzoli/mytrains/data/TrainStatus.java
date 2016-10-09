package com.manzolik.gmanzoli.mytrains.data;

import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class TrainStatus implements JSONPopulable{

    private int delay;
    private String trainDescription;
    private String lastCheckedStation;
    private Calendar lastUpdate;
    private boolean departed;
    private Calendar targetTime;
    private boolean targetPassed;
    private Calendar expectedDeparture;
    private int trainCode;
    private boolean suppressed;
    private Station targetStation;


    public int getTrainCode() {
        return trainCode;
    }

    public boolean isTargetPassed() {
        return targetPassed;
    }

    public int getDelay() {
        return delay;
    }

    public String getTrainDescription() {
        return trainDescription;
    }

    public String getLastCheckedStation() {
        return lastCheckedStation;
    }

    public Calendar getLastUpdate() {
        return lastUpdate;
    }

    public boolean isDeparted() {
        return departed;
    }

    public void setTargetStation(Station s){
        this.targetStation = s;
    }

    public Station getTargetStation() {
        return targetStation;
    }

    public Calendar getTargetTime() {
        return targetTime;
    }

    public Calendar getExpectedDeparture() {
        return expectedDeparture;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    @Override
    public void populate(JSONObject data) {

        // Controllo se il treno è stato soppresso
        /* Non sembra esserci un flag particolare che determini se il treno è soppresso o meno.
        * I valori testati sono quelli che vegnono trovati diversi dal solito quando un treno
        * è soppresso, possono quindi esserci degli errori.*/
        suppressed =  (
                data.optString("compDurata").equals("0:0") &&
                data.optJSONArray("fermate").length() == 0 &&
                data.optInt("provvedimento") == 1
        );

        // Partenza prevista per il treno
        expectedDeparture = Calendar.getInstance();
        expectedDeparture.setTime(new Date(data.optLong("orarioPartenza")));

        // Codice + Tipologia del treno
        String cat = data.optString("categoria");
        trainCode = data.optInt("numeroTreno");
        trainDescription = cat + " " + trainCode;

        lastCheckedStation = StringUtils.capitalizeString(data.optString("stazioneUltimoRilevamento"));

        departed = !(lastCheckedStation.equals("--"));
        lastUpdate = Calendar.getInstance();
        lastUpdate.setTime(new Date(data.optLong("oraUltimoRilevamento")));

        if (!departed){
            if (expectedDeparture.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                long expectedDelay = Calendar.getInstance().getTimeInMillis() - expectedDeparture.getTimeInMillis();

                delay = (int) expectedDelay / 60000; // Conversione in minuti
            } else {
                delay = 0;
            }
        } else {
            delay = data.optInt("ritardo");
        }

        targetTime = Calendar.getInstance();
        JSONArray stopsArray = data.optJSONArray("fermate");
        for (int i = 0; i < stopsArray.length(); i++) {
            try {
                JSONObject obj = stopsArray.getJSONObject(i);
                //System.out.println(obj);
                if (targetStation != null &&  obj.getString("id").equals(targetStation.getCode())) {
                    if (obj.getInt("actualFermataType") == 0) { // fermata ancora da prendere
                        this.targetPassed = false;
                        //System.out.println("Partenza teorica da StazioneTarget");
                        //System.out.println(obj.optLong("arrivo_teorico") + delay * 60000);
                        long tt = obj.optLong("arrivo_teorico") + delay * 60000;
                        targetTime.setTime(new Date(tt));
                        //System.out.println(targetTime.getTime().toString());

                    } else {
                        this.targetPassed = true;
                        targetTime.setTime(new Date(obj.getLong("partenzaReale")));
                    }

                    i = stopsArray.length() + 1; // BRUTTA COSA, sono una brutta persona

                } else {targetPassed = true;}
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
