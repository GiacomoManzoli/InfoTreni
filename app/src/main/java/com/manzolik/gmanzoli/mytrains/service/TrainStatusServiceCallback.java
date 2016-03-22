package com.manzolik.gmanzoli.mytrains.service;

import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import java.util.List;

public interface TrainStatusServiceCallback {
    void trainStatusServiceCallbackSuccess(List<TrainStatus> trains);
    void trainStatusServiceCallbackFailure(Exception exc);
}
