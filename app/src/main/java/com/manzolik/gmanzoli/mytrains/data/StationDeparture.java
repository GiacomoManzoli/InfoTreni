package com.manzolik.gmanzoli.mytrains.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;


public class StationDeparture implements JSONPopulable, Serializable {
    /*
    *     "numeroTreno": 20692,
    "categoria": "REG",
    "origine": null,
    "codOrigine": "S05706",
    "destinazione": "VENEZIA SANTA LUCIA",

     "binarioProgrammatoPartenzaDescrizione": "2              ",

     "binarioEffettivoPartenzaDescrizione": null, // null se non ancora definitivo o uguale a quello programmato
     "ritardo": 0,
     "compOrarioPartenza": "17:23",

     "compInStazionePartenza": [..] Array di stringhe vuote se non è ancora partiro, array di stringhe con scritto "partito" in varie lingue se è partito
    * */

    private String mTrainCode;
    private String mTrainCategory;
    private String mDestionationName;
    private String mDepartureCode;
    private String mExpectedTrack;
    private String mRealTrack;
    private Date mExpectedDeparture;
    private int mDelay;
    private boolean mDeparted;

    @Override
    public void populate(JSONObject data) {
        mTrainCode = data.optString("numeroTreno").trim();
        mTrainCategory = data.optString("categoria").trim();
        mDestionationName = data.optString("destinazione").trim();
        mDepartureCode = data.optString("codOrigine").trim();
        mExpectedTrack = data.optString("binarioProgrammatoPartenzaDescrizione").trim();
        mRealTrack = data.optString("binarioEffettivoPartenzaDescrizione").trim();

        String expectedDepartureString = data.optString("compOrarioPartenza");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(expectedDepartureString.split(":")[0]));
        c.set(Calendar.MINUTE, Integer.valueOf(expectedDepartureString.split(":")[1]));
        mExpectedDeparture = c.getTime();

        mDelay = data.optInt("ritardo");

        JSONArray compPartenza = data.optJSONArray("compInStazionePartenza");
        String firstElem = (String) compPartenza.opt(0);
        mDeparted = ! firstElem.equals("");
    }

    public String getTrainCode() {
        return mTrainCode;
    }

    public String getTrainCategory() {
        return mTrainCategory;
    }

    public String getDestionationName() {
        return mDestionationName;
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

    public Date getExpectedDeparture() {
        return mExpectedDeparture;
    }

    public int getDelay() {
        return mDelay;
    }

    public boolean isDeparted() {
        return mDeparted;
    }
}
