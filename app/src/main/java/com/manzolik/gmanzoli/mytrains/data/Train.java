package com.manzolik.gmanzoli.mytrains.data;

import java.util.Calendar;

/**
 * Created by gmanzoli on 26/02/16.
 */
public class Train {

    private int code;
    private Station departureStation;
    private Calendar departureTime;

    private String category;

    public Train(int code, Station departureStation, String category, Calendar departureTime) {
        this.code = code;
        this.departureStation = departureStation;
        this.category = category;
        this.departureTime = departureTime;
    }

    public int getCode() {
        return code;
    }
    public String getCategory() {
        return category;
    }
    public Station getDepartureStation() {
        return departureStation;
    }

    public Calendar getDepartureTime() {
        return departureTime;
    }
}
