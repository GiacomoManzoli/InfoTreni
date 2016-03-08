package com.manzolik.gmanzoli.mytrains.service;

import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import java.util.List;

/**
 * Created by gmanzoli on 26/02/16.
 */
public interface TrenitaliaServiceCallback {
    void serviceSuccess(List<TrainStatus> trains);
    void serviceFailure(Exception exc);
}
