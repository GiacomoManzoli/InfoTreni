package com.manzolik.gmanzoli.mytrains.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class StationArrival implements JSONPopulable, Serializable {
/*
*    "numeroTreno": 9430,
    "categoria": "ES*",
    "categoriaDescrizione": null,
    "origine": "NAPOLI CENTRALE",
    "codOrigine": "S09218",

    "binarioEffettivoArrivoDescrizione": "2",
    "binarioProgrammatoArrivoDescrizione": "2              ",
    "compOrarioArrivo": "17:07",
    "ritardo": 0,

    "compInStazioneArrivo": [..] Array di stringhe vuote se non è ancora arrivato, array di stringhe con scritto "arrivato" in varie lingue se è partito

    */
    private String mTrainCode;
    private String mTrainCategory;
    private String mDepartureName;
    private String mDepartureCode;
    private String mExpectedTrack;
    private String mRealTrack;
    private Date mExpectedArrival;
    private int mDelay;
    private boolean mArrived;

    @Override
    public void populate(JSONObject data) {
        mTrainCode = data.optString("numeroTreno").trim();
        mTrainCategory = data.optString("categoria").trim();
        mDepartureName = data.optString("origine").trim();
        mDepartureCode = data.optString("codOrigine").trim();
        mExpectedTrack = data.optString("binarioProgrammatoArrivoDescrizione").trim();
        mRealTrack = data.optString("binarioEffettivoArrivoDescrizione").trim();

        String expectedArrivalString = data.optString("compOrarioArrivo");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(expectedArrivalString.split(":")[0]));
        c.set(Calendar.MINUTE, Integer.valueOf(expectedArrivalString.split(":")[1]));
        mExpectedArrival = c.getTime();

        mDelay = data.optInt("ritardo");

        JSONArray compArrivo = data.optJSONArray("compInStazioneArrivo");
        String firstElem = (String) compArrivo.opt(0);
        mArrived = ! firstElem.equals("");
    }

    public String getTrainCode() {
        return mTrainCode;
    }
    public String getTrainCategory() {
        return mTrainCategory;
    }
    public String getDepartureName() {
        return mDepartureName;
    }
    public String getDepartureCode() {
        return mDepartureCode;
    }
    public String getExpectedTrack() {
        return mExpectedTrack;
    }
    public String getRealTrack() {
        return mRealTrack;
    }
    public Date getExpectedArrival() {
        return mExpectedArrival;
    }
    public int getDelay() {
        return mDelay;
    }
    public boolean isArrived() { return mArrived; }
}
