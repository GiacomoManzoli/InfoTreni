package com.manzolik.gmanzoli.mytrains.service;

import com.manzolik.gmanzoli.mytrains.data.Station;

import java.util.List;


public interface TrainStopsServiceCallback {
    void trainStopsServiceCallbackSuccess(List<String> stationNameList);
    void trainStopsServiceCallbackFailure(Exception exc);
}
