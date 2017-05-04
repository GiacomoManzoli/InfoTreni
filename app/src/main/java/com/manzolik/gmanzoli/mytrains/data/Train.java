package com.manzolik.gmanzoli.mytrains.data;


import java.io.Serializable;

public class Train implements Serializable{

    private final String code;
    private final Station departureStation;
    private final int id;
    public Train(int id, String code, Station departureStation) {
        this.code = code;
        this.departureStation = departureStation;
        this.id = id;
    }

    public String getCode() {
        return code;
    }
    public Station getDepartureStation() {
        return departureStation;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", code, departureStation);
    }
}
