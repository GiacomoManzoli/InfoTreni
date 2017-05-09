package com.manzolik.gmanzoli.mytrains.data;

import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class StationDeparture implements JSONPopulable, Serializable, StationInfo {
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
        mDestionationName = StringUtils.capitalizeString(data.optString("destinazione").trim());
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

    @Override
    public String getTrainCode() {
        return mTrainCode;
    }
    public String getTrainCategory() {
        return mTrainCategory;
    }
    public String getDestionationName() {
        return mDestionationName;
    }
    @Override
    public String getTrainDepartureCode() {
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

    @Override
    public String getTrainDescription() {
        return mTrainCategory + " " + mTrainCode;
    }

    @Override
    public String getTrainTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(mExpectedDeparture);
    }

    @Override
    public String getTrainDelay() {
        String sing = "";
        sing = (mDelay > 0)? "+":sing;
        sing = (mDelay < 0)? "-":sing;

        return sing+String.valueOf(Math.abs(mDelay));
    }

    @Override
    public String getTrainInfo() {
        return "Per " + mDestionationName;
    }

    @Override
    public String getTrainExpectedTrack() {
        return mExpectedTrack;
    }

    @Override
    public String getTrainRealTrack() {
        return mRealTrack;
    }

    @Override
    public boolean isActive() {
        return mDeparted;
    }
}
