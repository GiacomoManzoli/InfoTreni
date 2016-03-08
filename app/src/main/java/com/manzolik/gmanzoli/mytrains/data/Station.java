package com.manzolik.gmanzoli.mytrains.data;

public class Station {

    private String code;
    private String name;

    public Station(String name) {
        this.name = name;
        this.code ="";
    }

    public Station(String name, String code) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
