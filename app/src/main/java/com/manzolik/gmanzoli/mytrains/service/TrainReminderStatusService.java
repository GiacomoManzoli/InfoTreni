package com.manzolik.gmanzoli.mytrains.service;


import com.manzolik.gmanzoli.mytrains.data.TrainReminder;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Classe che si occupa di recuperare lo stato dei treni presenti in una lista di reminder.
 */

public class TrainReminderStatusService implements TrainStatusService.TrainStatusServiceListener {

    private static final String TAG = TrainReminderStatusService.class.getSimpleName();

    private List<TrainStatus> trainStatusList; //lista con i risultati parziali
    private boolean queryInProgress = false;
    private int callbackCount;

    private TrainReminderStatusServiceListener mListener;


    public void getTrainStatusList(List<TrainReminder> reminderList, final TrainReminderStatusServiceListener listener){
        this.mListener = listener;
        if (queryInProgress) {
            mListener.onTrainReminderStatusSerivceFailure(new TrainReminderStatusService.QueryInProgressException("C'è già una query in esecuzione"));
            return;
        }
        queryInProgress = true;

        List<TrainReminder> trainList = new ArrayList<>(); //Deve contenere i treni da richiedere
        Calendar currentTime = Calendar.getInstance();
        // Filtra la lista dei reminder per evitare di effettuare chiamate inutili
        for(TrainReminder tr: reminderList){
            if (tr.shouldShowReminder(currentTime) && !trainInReminderList(tr.getTrain().getCode(), trainList)) {
                trainList.add(tr);
            }
        }

        trainStatusList = new ArrayList<>();
        if (trainList.size() == 0){ // non ci sono chiamate da fare
            queryInProgress = false;
            mListener.onTrainReminderStatusServiceSuccess(trainStatusList);
        } else{
            callbackCount = trainList.size();
            for (TrainReminder tr: trainList) {
                TrainStatusService tss = new TrainStatusService();
                tss.getStatusForTrainReminder(tr, this);
            }
        }
    }


    private boolean trainInReminderList(String trainCode, List<TrainReminder> reminders){
        for(int i = 0; i < reminders.size(); i++){
            if (reminders.get(i).getTrain().getCode().equals(trainCode)){
                return  true;
            }
        }
        return false;
    }


    /*
    * Implementazione di TrainStatusService.TrainStatusServiceListener
    * */
    @Override
    public void onTrainStatusSuccess(TrainStatus ts) {
        // Funzione che viene invocata quando sono stati ottenuti i risultati per un treno
        trainStatusList.add(ts);
        callbackCount--;
        if (callbackCount == 0){
            //Ho tutti i risultati
            queryInProgress = false;
            mListener.onTrainReminderStatusServiceSuccess(trainStatusList);
        }
    }

    @Override
    public void onTrainStatusFailure(Exception e) {
        // Funzione che viene invocata se non è stato possibile ottenere il risultato per un treno
        queryInProgress = false;
        mListener.onTrainReminderStatusSerivceFailure(e);
    }

    /*
    * Classi interne
    * */
    public interface TrainReminderStatusServiceListener{
        void onTrainReminderStatusServiceSuccess(List<TrainStatus> trainStatuses);
        void onTrainReminderStatusSerivceFailure(Exception e);
    }

    public class QueryInProgressException extends Exception{
        QueryInProgressException(String detailMessage) {
            super(detailMessage);
        }
    }

}
