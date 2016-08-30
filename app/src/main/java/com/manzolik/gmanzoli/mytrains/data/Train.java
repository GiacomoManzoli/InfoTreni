package com.manzolik.gmanzoli.mytrains.data;

import java.util.Calendar;

public class Train {

    private final int code;
    private final Station departureStation;
    private final int id;
    public Train(int id, int code, Station departureStation) {
        this.code = code;
        this.departureStation = departureStation;
        this.id = id;
    }

    public int getCode() {
        return code;
    }
    public Station getDepartureStation() {
        return departureStation;
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%d - %s", code, departureStation);
    }
}
