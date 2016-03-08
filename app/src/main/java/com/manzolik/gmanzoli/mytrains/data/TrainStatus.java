package com.manzolik.gmanzoli.mytrains.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gmanzoli on 26/02/16.
 */
public class TrainStatus implements JSONPopulable{

    private int delay;
    private Train train;
    private Station lastCheckedStation;
    private Calendar lastUpdate;
    private boolean departed;
    private final TrainReminder associatedReminder;
    private Calendar targetTime;
    private boolean targetPassed;

    public TrainStatus(TrainReminder associatedReminder) {
        this.associatedReminder = associatedReminder;
    }

    public boolean isTargetPassed() {
        return targetPassed;
    }

    public int getDelay() {
        return delay;
    }

    public Train getTrain() {
        return train;
    }

    public Station getLastCheckedStation() {
        return lastCheckedStation;
    }

    public Calendar getLastUpdate() {
        return lastUpdate;
    }

    public boolean isDeparted() {
        return departed;
    }

    public Station getTargetStation() { return associatedReminder.getTargetStaion();}

    public Calendar getTargetTime() {
        return targetTime;
    }

    @Override
    public void populate(JSONObject data) {
        Calendar departureTime = Calendar.getInstance();
        departureTime.setTime(new Date(data.optLong("orarioPartenza")));

        Station trainStation = new Station(data.optString("origine"), data.optString("idOrigine"));

        train = new Train(data.optInt("numeroTreno"), trainStation, data.optString("categoria"),departureTime);
        lastCheckedStation = new Station(data.optString("stazioneUltimoRilevamento"));
        departed = !(lastCheckedStation.getName().equals("--"));
        lastUpdate = Calendar.getInstance();
        lastUpdate.setTime(new Date(data.optLong("oraUltimoRilevamento")));

        if (!departed){
            long expectedDelay = Calendar.getInstance().getTimeInMillis() - train.getDepartureTime().getTimeInMillis();
            delay = (int) expectedDelay / 60000; // Conversione in minuti
        } else {
            delay = data.optInt("ritardo");
        }

        Station targetStation = associatedReminder.getTargetStaion();
        targetTime = Calendar.getInstance();
        JSONArray stopsArray = data.optJSONArray("fermate");
        for (int i = 0; i < stopsArray.length(); i++) {
            try {
                JSONObject obj = stopsArray.getJSONObject(i);
                System.out.println(obj);
                if (obj.getString("id").equals(targetStation.getCode())){

                    if (obj.getInt("actualFermataType") == 0){ // fermata ancora da prendere
                        this.targetPassed = false;
                        System.out.println("Partenza teorica da StazioneTarget");
                        System.out.println(obj.optLong("partenza_teorica") + delay * 60000);
                        long tt = obj.optLong("partenza_teorica") + delay * 60000;
                        targetTime.setTime(new Date(tt));
                        System.out.println(targetTime.getTime().toString());

                    } else {
                        this.targetPassed = true;
                        targetTime.setTime(new Date(obj.getLong("partenzaReale")));
                    }

                    i = stopsArray.length() +1; // BRUTTA COSA, sono una brutta persona

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


}
