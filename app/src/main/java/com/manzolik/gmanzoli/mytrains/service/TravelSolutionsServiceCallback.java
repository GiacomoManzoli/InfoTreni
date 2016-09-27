package com.manzolik.gmanzoli.mytrains.service;

import com.manzolik.gmanzoli.mytrains.data.TravelSolution;

import java.util.List;

public interface TravelSolutionsServiceCallback {
    void travelSolutionsServiceCallbackSuccess(List<TravelSolution> solutions);
    void travelSolutionsServiceCallbackFailure(Exception exc);
}
