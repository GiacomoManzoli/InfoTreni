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

    public boolean getTrainStatusList(List<TrainReminder> reminderList, final TrainReminderStatusServiceListener listener){
        if (mQueryInProgress) {
            return false;
        }
        mQueryInProgress = true;
        this.mListener = listener;

        mTrainStatusList = new ArrayList<>();
        if (reminderList.size() == 0){ // Non ci sono chiamate da fare
            mQueryInProgress = false;
            mListener.onTrainReminderStatusServiceSuccess(mTrainStatusList);
        } else{
            mCallbackCount = reminderList.size();
            for (TrainReminder tr: reminderList) {
                TrainStatusService tss = new TrainStatusService();
                tss.getStatusForTrainReminder(tr, this);
            }
        }
        return true;
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
}
