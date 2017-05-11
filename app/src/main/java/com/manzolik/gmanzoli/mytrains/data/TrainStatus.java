package com.manzolik.gmanzoli.mytrains.data;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Note: prima di chiamare populate è necessario impostare il target
 * */
public class TrainStatus implements JSONPopulable, Serializable {


    public enum TrainStatusInfo {
        STATUS_REGULAR,
        STATUS_SUPPRESSED,
        STATUS_PARTIALLY_SUPPRESSED,
        STATUS_DEVIATED,
        STATUS_UNKNOWN
    }

    private int delay;
    private String trainDescription;
    private String lastCheckedStation;
    private String departureStationName;
    private String arrivalStationName;
    private Calendar lastUpdate;
    private boolean departed;
    private Calendar targetTime;
    private boolean targetPassed;
    private Calendar expectedDeparture;
    private Calendar expectedArrival;
    private String trainCode;
    private Station targetStation;
    private String departureStationCode;
    private String arrivalStationCode;
    private String extraInfo; // es: "Treno cancellato da VENEZIA SANTA LUCIA a VENEZIA MESTRE. Parte da VENEZIA MESTRE."
    private List<TrainStop> stops;

    private TrainStatusInfo trainStatusInfo;

    public String getDepartureStationCode() {
        return departureStationCode;
    }
    public String getArrivalStationCode() {
        return arrivalStationCode;
    }

    public String getTrainCode() {
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

    public boolean isArrivedAtEnd() {
        TrainStop lastStop = stops.get(stops.size() -1);
        return lastStop.getKind() == TrainStop.TrainStopKind.ARRIVAL
                && lastStop.trainArrived();
    }

    public String getDepartureStationName() {
        return departureStationName;
    }

    public String getArrivalStationName() {
        return arrivalStationName;
    }

    public Calendar getExpectedArrival() {
        return expectedArrival;
    }

    public TrainStatusInfo getTrainStatusInfo() {
        return trainStatusInfo;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public List<TrainStop> getStops() {
        return stops;
    }

    private TrainStatusInfo inferTrainStatusInfo(JSONObject data) {
        /*
        tipoTreno vale 'PG' e provvedimento vale 0: treno regolare
        tipoTreno vale 'ST' e provvedimento vale 1: treno soppresso (in questo caso l'array fermate ha lunghezza 0)
        tipoTreno vale 'PP' oppure 'SI' oppure 'SF' e provvedimento vale 0 oppure 2: treno parzialmente soppresso (in questo caso uno o più elementi dell'array fermate hanno il campo actualFermataType uguale a 3)
        tipoTreno vale 'DV' e provvedimento vale 3: treno deviato (da approfondire)
        * */
        String tipoTreno = data.optString("tipoTreno");
        int provvedimento = data.optInt("provvedimento");

        switch (tipoTreno) {
            case "PG":
                if (provvedimento == 0) {
                    return TrainStatusInfo.STATUS_REGULAR;
                }
                break;
            case "ST":
                if (provvedimento == 1) {
                    if (BuildConfig.DEBUG) {
                        if (data.optJSONArray("fermate").length() != 0) {
                            throw new AssertionError();
                        }
                    }
                    return TrainStatusInfo.STATUS_SUPPRESSED;
                }
                break;
            case "PP":
            case "SI":
            case "SF":
                if (provvedimento == 0 || provvedimento == 2) {
                    return TrainStatusInfo.STATUS_PARTIALLY_SUPPRESSED;
                }
                break;
            case "DV":
                return TrainStatusInfo.STATUS_DEVIATED;
        }
        return TrainStatusInfo.STATUS_UNKNOWN;
    }

    @Override
    public void populate(JSONObject data) {


        trainStatusInfo = inferTrainStatusInfo(data);

        // Partenza prevista per il treno
        expectedDeparture = Calendar.getInstance();
        expectedDeparture.setTime(new Date(data.optLong("orarioPartenza")));
        // Arrivo a destinazione previsto per il treno
        expectedArrival = Calendar.getInstance();
        expectedArrival.setTime(new Date(data.optLong("orarioArrivo")));
        // Stazione di partenza (nome e codice)
        departureStationName = StringUtils.capitalizeString(data.optString("origine"));
        departureStationCode = data.optString("idOrigine");
        // Stazione di arrivo (nome e codice)
        arrivalStationName = StringUtils.capitalizeString(data.optString("destinazione"));
        arrivalStationCode = data.optString("idDestinazione");

        // Codice + Tipologia del treno
        String cat = data.optString("categoria");
        trainCode = data.optString("numeroTreno");
        trainDescription = cat + " " + trainCode; // = compNumeroTreno

        // Informazioni extra
        extraInfo = data.optString("subTitle");

        // Stazione e ora ultimo rilevamento
        String stazioneUltimoRilevamento = data.optString("stazioneUltimoRilevamento");
        // stazioneUltimoRilevamento = "--" se non è ancora stato rilevato
        if (stazioneUltimoRilevamento.equals("--")) {
            lastCheckedStation = null;
            lastUpdate = null;
            departed = false;
        } else {
            departed = true;
            lastCheckedStation = StringUtils.capitalizeString(stazioneUltimoRilevamento);
            lastUpdate = null;
            long oraUltimoRilevamento = data.optLong("oraUltimoRilevamento", -1);
            if (oraUltimoRilevamento != -1) {
                lastUpdate = Calendar.getInstance();
                lastUpdate.setTime(new Date(oraUltimoRilevamento));
            }
        }

        if (departed) {
            delay = data.optInt("ritardo");
        }
        else {
            if (expectedDeparture.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                long expectedDelay = Calendar.getInstance().getTimeInMillis() - expectedDeparture.getTimeInMillis();
                delay = (int) expectedDelay / 60000; // Conversione in minuti
            } else {
                delay = 0;
            }
        }

        targetTime = Calendar.getInstance();
        JSONArray stopsArray = data.optJSONArray("fermate");
        stops = new ArrayList<>();
        for (int i = 0; i < stopsArray.length(); i++) {

            JSONObject obj = stopsArray.optJSONObject(i);
            TrainStop stop = new TrainStop();
            stop.populate(obj);
            stops.add(stop);
            if (targetStation != null && stop.getStationCode().equals(targetStation.getCode())) {
                // La fermata corrente è quella target
                if (stop.trainArrived()) {
                    targetTime.setTime(stop.getArrivalExpected());
                } else {
                    long tt = stop.getArrivalExpected().getTime();
                    if (departed) {
                        tt += delay * 60000;
                    }
                    targetTime.setTime(new Date(tt));
                }
                targetPassed = stop.trainLeaved();
            }


        }
    }

}
