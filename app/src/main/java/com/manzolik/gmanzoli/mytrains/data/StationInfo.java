package com.manzolik.gmanzoli.mytrains.data;


/*
    Inferfaccia comune tra StationDeparture e StationArrival
*/
public interface StationInfo extends JSONPopulable{
    String getTrainCode();
    String getTrainDepartureCode();
    String getTrainDescription();
    String getTrainTime();
    String getTrainDelay();
    String getTrainInfo();
    String getTrainExpectedTrack();
    String getTrainRealTrack();
    boolean isActive();
}
