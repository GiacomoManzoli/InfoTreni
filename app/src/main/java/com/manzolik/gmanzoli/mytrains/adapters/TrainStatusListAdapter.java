package com.manzolik.gmanzoli.mytrains.adapters;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TrainStatusListAdapter
        extends RecyclerView.Adapter<TrainStatusListAdapter.TrainStatusItemHolder> {

    private static final String TAG = TrainStatusListAdapter.class.getSimpleName();
    private List<TrainStatus> mTrainStatusList;

    private OnStatusSelectListener mListener;

    public TrainStatusListAdapter(List<TrainStatus> trainStatusList) {
        this.mTrainStatusList = trainStatusList;

    }

    @Override
    public TrainStatusItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_adapter_row_train_status, parent, false);
        return new TrainStatusItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TrainStatusItemHolder holder, int position) {
        final TrainStatus status = mTrainStatusList.get(position);

        holder.trainCodeTextView.setText(status.getTrainDescription());


        if (status.getTrainStatusInfo() == TrainStatus.TrainStatusInfo.STATUS_SUPPRESSED) {
            holder.trainDelayTextView.setText("Soppresso");
        } else if (status.getTrainStatusInfo() == TrainStatus.TrainStatusInfo.STATUS_REGULAR) {
            holder.trainDelayTextView.setText(String.format(Locale.getDefault(),
                    "Ritardo %d'",
                    status.getDelay()));
        } else {
            holder.trainDelayTextView.setText(String.format(Locale.getDefault(),
                    "Modificato - Ritardo %d'",
                    status.getDelay()));

        }



        holder.trainDelayTextView.setVisibility(TextView.VISIBLE);
        // Stringa relativa all'ultimo aggiornamento dei dati
        String lastUpdate;
        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());

        if (status.isDeparted()){
            // Il treno è già partito, mostro la stringa con le informazioni relative all'ultimo aggiornamento
            lastUpdate = String.format("%s - %s", status.getLastCheckedStation(), format.format(status.getLastUpdate().getTime()));
        }else {
            // Calcolo la partenza prevista
            lastUpdate =  String.format("Non parito, partenza prevista alle %s", format.format(status.getExpectedDeparture().getTime()));
            if (status.getDelay() < 0){
                holder.trainDelayTextView.setVisibility(TextView.INVISIBLE);
            }
        }
        holder.trainLastUpdateTextView.setText(lastUpdate);


        // Colore per la textBox del ritardo
        if (status.getDelay() > 0 ||
                status.getTrainStatusInfo() != TrainStatus.TrainStatusInfo.STATUS_REGULAR){
            holder.trainDelayTextView.setTextColor(Color.RED);
        } else {
            holder.trainDelayTextView.setTextColor(0x388E3C); //Verde scuro
        }

        // Gestione della stazione target
        holder.trainTargetTextView.setText(String.format("%s - %s", status.getTargetStation().getName(), format.format(status.getTargetTime().getTime())));
        if (status.isTargetPassed()){
            holder.trainTargetTextView.setVisibility(TextView.GONE);
            holder.trainTargetHeaderTextView.setVisibility(TextView.GONE);
        } else {
            holder.trainTargetTextView.setVisibility(TextView.VISIBLE);
            holder.trainTargetHeaderTextView.setVisibility(TextView.VISIBLE);
        }

        
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick - " + status.toString());
                if (mListener != null) {
                    mListener.onStatusSelected(status);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mTrainStatusList.size();
    }


    public void setItems(List<TrainStatus> trainStatuses){
        this.mTrainStatusList = trainStatuses;
        this.notifyDataSetChanged();
    }



    static class TrainStatusItemHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView trainCodeTextView;
        final TextView trainDelayTextView;
        final TextView trainLastUpdateTextView;
        final TextView trainTargetHeaderTextView;
        final TextView trainTargetTextView;


        TrainStatusItemHolder(View v) {
            super(v);
            this.view = v;
            this.trainCodeTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_train_code);
            this.trainDelayTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_delay);
            this.trainLastUpdateTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_last_update);
            this.trainTargetHeaderTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_expected_arrive);
            this.trainTargetTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_station);
        }
    }

    public interface OnStatusSelectListener {
        void onStatusSelected(TrainStatus status);
    }

    public void setOnStatusSelectListener(OnStatusSelectListener listener) {
        this.mListener = listener;
    }

    public void removeOnStatusSelectListener() {
        this.mListener = null;
    }
}
