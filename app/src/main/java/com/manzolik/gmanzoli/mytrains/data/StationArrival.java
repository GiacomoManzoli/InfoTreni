package com.manzolik.gmanzoli.mytrains.data;

import com.manzolik.gmanzoli.mytrains.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StationArrival implements JSONPopulable, Serializable, StationInfo {
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
        mDepartureName = StringUtils.capitalizeString(data.optString("origine").trim());
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


    @Override
    public String getTrainCode() {
        return mTrainCode;
    }
    public String getTrainCategory() {
        return mTrainCategory;
    }
    public String getExpectedTrack() {
        return mExpectedTrack;
    }
    public String getRealTrack() {
        return mRealTrack;
    }
    public String getDepartureName() {
        return mDepartureName;
    }
    @Override
    public String getTrainDepartureCode() {
        return mDepartureCode;
    }
    public Date getExpectedArrival() { return mExpectedArrival;}
    public int getDelay() {
        return mDelay;
    }
    public boolean isArrived() {
        return mArrived;
    }

    @Override
    public String getTrainDescription() {
        return mTrainCategory + " " + mTrainCode;
    }

    @Override
    public String getTrainTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(mExpectedArrival);
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
        return "Da " + mDepartureName;
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
        return mArrived;
    }
}
