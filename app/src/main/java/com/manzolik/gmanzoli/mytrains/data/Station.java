package com.manzolik.gmanzoli.mytrains.data;

public class Station {

    // name;id;region;region_code;city;lat;lon

    private final String code;
    private final String name;
    private final String region;
    private final int regionCode;
    private final String city;
    private final double latitude;
    private final double longitude;
    private final int id;



    public Station(int id, String name, String code, String region, int regionCode, String city, double latitude, double longitude) {
        this.code = code;
        this.name = name;
        this.region = region;
        this.regionCode = regionCode;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public int getID() {
        return id;
    }

    public int getRegionCode() {
        return regionCode;
    }

    public String getCity() {
        return city;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
