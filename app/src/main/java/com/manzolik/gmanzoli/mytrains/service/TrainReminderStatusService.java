package com.manzolik.gmanzoli.mytrains.service;


import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 * Classe che si occupa di recuperare lo stato dei treni presenti in una lista di reminder.
 */

public class TrainReminderStatusService implements TrainStatusService.TrainStatusServiceListener {

    private static final String TAG = TrainReminderStatusService.class.getSimpleName();

    private List<TrainStatus> mTrainStatusList; //lista con i risultati parziali
    private boolean mQueryInProgress = false;
    private int mCallbackCount;

    private TrainReminderStatusServiceListener mListener;


    public void getTrainStatusList(List<TrainReminder> reminderList, final TrainReminderStatusServiceListener listener){
        if (mQueryInProgress) {
            mListener.onTrainReminderStatusServiceFailure(new QueryInProgressException("C'è già una query in esecuzione"));
            return;
        }

        this.mListener = listener;
        mQueryInProgress = true;

        List<TrainReminder> trainList = TrainReminder.filterByShouldShow(reminderList);

        mTrainStatusList = new ArrayList<>();
        if (trainList.size() == 0){ // Non ci sono chiamate da fare
            mQueryInProgress = false;
            mListener.onTrainReminderStatusServiceSuccess(mTrainStatusList);
        } else{
            mCallbackCount = trainList.size();
            for (TrainReminder tr: trainList) {
                TrainStatusService tss = new TrainStatusService();
                tss.getStatusForTrainReminder(tr, this);
            }
        }
    }

    /*
    * Implementazione di TrainStatusService.TrainStatusServiceListener
    * */
    @Override
    public void onTrainStatusSuccess(TrainStatus ts) {
        // Funzione che viene invocata quando sono stati ottenuti i risultati per un treno
        mTrainStatusList.add(ts);
        mCallbackCount--;
        if (mCallbackCount == 0){
            // Ho tutti i risultati
            mQueryInProgress = false;
            mListener.onTrainReminderStatusServiceSuccess(mTrainStatusList);
        }
    }

    @Override
    public void onTrainStatusFailure(Exception e) {
        // Funzione che viene invocata se non è stato possibile ottenere il risultato per un treno
        mQueryInProgress = false;
        mListener.onTrainReminderStatusServiceFailure(e);
    }

    /*
    * Classi interne
    * */
    public interface TrainReminderStatusServiceListener{
        void onTrainReminderStatusServiceSuccess(List<TrainStatus> trainStatuses);
        void onTrainReminderStatusServiceFailure(Exception e);
    }

    public class QueryInProgressException extends Exception{
        QueryInProgressException(String detailMessage) {
            super(detailMessage);
        }
    }

}
