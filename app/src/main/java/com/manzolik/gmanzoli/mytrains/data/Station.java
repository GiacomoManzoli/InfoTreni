package com.manzolik.gmanzoli.mytrains.data;

import android.location.Location;

import java.io.Serializable;
import java.util.List;

public class Station implements Serializable {

    // name;id;region;region_code;city;lat;lon

    private final String code;
    private final String name;
    private final String region;
    private final int regionCode;
    private final String city;
    private final double latitude;
    private final double longitude;
    private final int id;
    private List<StationArrival> arrivals;
    private List<StationDeparture> departures;

    public Station(int id, String name, String code, String region, int regionCode, String city, double latitude, double longitude) {
        this.code = code;
        this.name = name;
        this.region = (region == null)? "" : region;
        this.regionCode = regionCode;
        this.city = (city == null)? "" : city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }

    public Station(String newStationName, String newStationCode) {
        this.code = newStationCode;
        this.name = newStationName;
        this.region = "";
        this.regionCode = -1;
        this.city = "";
        this.latitude = 0;
        this.longitude = 0;
        this.id = -1;
    }

    public boolean isMaintenanceRequired() {
        return this.getRegion() == null
                || this.getCity() == null
                || this.getLongitude() == 0
                || this.getLatitude() == 0
                || this.getRegionCode() == -1;
    }

    public List<StationArrival> getArrivals() {
        return arrivals;
    }

    public void setArrivals(List<StationArrival> arrivals) {
        this.arrivals = arrivals;
    }

    public List<StationDeparture> getDepartures() {
        return departures;
    }

    public void setDepartures(List<StationDeparture> departures) {
        this.departures = departures;
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

    public int getId() {
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

    public double distanceFromLocation(Location location) {
        return distanceFromLatLong(location.getLatitude(), location.getLongitude());
    }

    public double distanceFromStation(Station s) {
        return distanceFromLatLong(s.getLatitude(), s.getLongitude());
    }

    private double distanceFromLatLong(double lat, double lon) {
        double distance = Math.pow(latitude - lat, 2) +
                Math.pow(longitude - lon, 2);
        return Math.sqrt(distance);
    }


    @Override
    public String toString() {
        return name;
    }

}
