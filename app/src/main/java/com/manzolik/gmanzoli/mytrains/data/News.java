package com.manzolik.gmanzoli.mytrains.data;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class News implements Serializable, JSONPopulable {

    private String mTitle;
    private String mText;
    private Date mDate;

    public String getTitle() {
        return mTitle;
    }

    public String getText() {
        return mText;
    }

    public Date getDate() {
        return mDate;
    }

    @Override
    public void populate(JSONObject data) {
        mTitle = data.optString("titolo").trim();
        mText = data.optString("testo").trim();
        mDate = new Date(data.optLong("data"));
    }
}
