package com.manzolik.gmanzoli.mytrains.data;

import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONObject;

import java.util.Date;


public class TrainStop implements JSONPopulable{
    public enum TrainStopKind {
        DEPARTURE,
        INTERMEDIATE,
        ARRIVAL,
        SKIPPED
    }
    public enum TrainStopStatus {
        REGULAR,
        NOT_SCHEDULED,
        SUPPRESSED,
        UNKNOWN
    }

    private String mStationCode;
    private String mStationName;
    private int mArrivalDelay;
    private int mDepartureDelay;
    private TrainStopKind mKind;
    private TrainStopStatus mStatus;
    private String mDepartureTrackExpected;
    private String mDepartureTrack;

    private boolean mArrived;
    private boolean mLeaved;

    private Date mArrivalExpected;
    private Date mArrival;
    private Date mDepartureExpected;
    private Date mDeparture;

    public Date getArrivalExpected() {
        return mArrivalExpected;
    }

    public Date getArrival() {
        return mArrival;
    }

    public Date getDepartureExpected() {
        return mDepartureExpected;
    }

    public Date getDeparture() {
        return mDeparture;
    }

    public boolean trainArrived() {
        return mArrived;
    }

    public boolean trainLeaved() {
        return mLeaved;
    }

    public String getStationCode() {
        return mStationCode;
    }

    public String getStationName() {
        return mStationName;
    }

    public int getArrivalDelay() {
        return mArrivalDelay;
    }

    public int getDepartureDelay() {
        return mDepartureDelay;
    }

    public TrainStopKind getKind() {
        return mKind;
    }

    public TrainStopStatus getStatus() {
        return mStatus;
    }

    public String getDepartureTrackExpected() {
        return mDepartureTrackExpected;
    }

    public String getDepartureTrack() {
        return mDepartureTrack;
    }

    @Override
    public void populate(JSONObject data) {
        mStationCode = data.optString("id");
        mStationName = StringUtils.capitalizeString(data.optString("stazione"));
        mArrivalDelay = data.optInt("ritardoArrivo");
        mDepartureDelay = data.optInt("ritardoPartenza");

        mDepartureTrackExpected = data.optString("binarioProgrammatoPartenzaDescrizione").trim();
        // Se il binario Expected non è diverso da quello effettivo
        // binarioEffettivoPartenzaDescrizione è null.
        String binarioEffettivoPartenzaDescrizione = data.optString("binarioEffettivoPartenzaDescrizione").trim();
        mDepartureTrack = binarioEffettivoPartenzaDescrizione.equals("null")? null : binarioEffettivoPartenzaDescrizione;


        mKind = inferKind(data);
        mStatus = inferStatus(data);

        String arrivoReale = data.optString("arrivoReale", null);
        mArrived = arrivoReale != null && !arrivoReale.equals("null");

        String partenzaReale = data.optString("partenzaReale", null);
        mLeaved = partenzaReale != null && !partenzaReale.equals("null");

        long arrivo_teorico = data.optLong("arrivo_teorico", -1);
        mArrivalExpected = new Date(arrivo_teorico);
        mArrival = new Date(data.optLong("arrivoReale", -1));
        mDepartureExpected = new Date(data.optLong("partenza_teorica", -1));
        mDeparture = new Date(data.optLong("partenzaReale", -1));
    }



    private TrainStopStatus inferStatus(JSONObject data) {
        switch (data.optInt("actualFermataType")) {
            case 1:
                return TrainStopStatus.REGULAR;
            case 2:
                return TrainStopStatus.NOT_SCHEDULED;
            case 3:
                return TrainStopStatus.SUPPRESSED;
            case 0:
            default:
                return TrainStopStatus.UNKNOWN;
        }
    }

    private TrainStopKind inferKind(JSONObject data) {
        switch (data.optString("tipoFermata")) {
            case "P":
                return TrainStopKind.DEPARTURE;
            case "A":
                return TrainStopKind.ARRIVAL;
            case "F":
                return TrainStopKind.INTERMEDIATE;
            case "":
            default:
                return TrainStopKind.SKIPPED;
        }
    }

}