package com.manzolik.gmanzoli.mytrains.service;

import com.manzolik.gmanzoli.mytrains.data.Station;

import java.util.List;

public interface TrainDepartureStationServiceCallback {
    void trainDepartureStationServiceCallbackSuccess(List<Station> trains);
    void trainDepartureStationServiceCallbackFailure(Exception exc);
}