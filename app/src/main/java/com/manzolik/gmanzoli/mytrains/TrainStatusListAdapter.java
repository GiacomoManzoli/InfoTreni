package com.manzolik.gmanzoli.mytrains;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.data.TrainStatus;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TrainStatusListAdapter extends RecyclerView.Adapter<TrainStatusListAdapter.TrainStatusItemHolder> {

    private List<TrainStatus> trainStatusList;

    public TrainStatusListAdapter(List<TrainStatus> trainStatusList) {
        this.trainStatusList = trainStatusList;

    }

    @Override
    public TrainStatusItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_adapter_row_train_status, parent, false);

        return new TrainStatusItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrainStatusItemHolder holder, int position) {
        TrainStatus status = trainStatusList.get(position);


        holder.trainCodeTextView.setText(status.getTrainDescription());
        holder.trainDelayTextView.setText(String.format("Ritardo: %d", status.getDelay()));
        holder.trainStationTextView.setText(status.getLastCheckedStation());

        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());
        holder.trainTimeTextView.setText(format.format(status.getLastUpdate().getTime()));

        holder.trainTimeTextView.setVisibility(TextView.VISIBLE);
        holder.trainDelayTextView.setVisibility(TextView.VISIBLE);



        //Treno non partito
        if (!status.isDeparted()){
            // Nasconde l'orario dell'ultimo rilevamento
            holder.trainStationTextView.setText(R.string.not_departed);
            // Partenza prevista
            holder.trainTimeTextView.setText(String.format("Partenza prevista: %s", format.format(status.getExpectedDeparture().getTime())));
            if (status.getDelay() < 0){
                holder.trainDelayTextView.setVisibility(TextView.INVISIBLE);
            }
        }

        // Colore per la textBox del ritardo
        if (status.getDelay() > 0){
            holder.trainDelayTextView.setTextColor(Color.RED);
        } else {
            holder.trainDelayTextView.setTextColor(0x388E3C); //Verde scuro
        }

        // Gestione della stazione target
        holder.trainTargetTextView.setText(String.format("Arrivo a %s previsto: %s", status.getTargetStation().getName(), format.format(status.getTargetTime().getTime())));
        if (status.isTargetPassed()){
            holder.trainTargetTextView.setVisibility(TextView.GONE);
        } else {
            holder.trainTargetTextView.setVisibility(TextView.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return this.trainStatusList.size();
    }


    public void setItems(List<TrainStatus> trainStatuses){
        this.trainStatusList = trainStatuses;
        this.notifyDataSetChanged();
    }

    public static class TrainStatusItemHolder extends RecyclerView.ViewHolder {
        protected final TextView trainCodeTextView;
        protected final TextView trainDelayTextView;
        protected final TextView trainStationTextView;
        protected final TextView trainTimeTextView;
        protected final TextView trainTargetTextView;


        public TrainStatusItemHolder(View v) {
            super(v);
            this.trainCodeTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_train_code);
            this.trainDelayTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_delay);
            this.trainStationTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_last_update);
            this.trainTimeTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_last_time_update);
            this.trainTargetTextView = (TextView) v.findViewById(R.id.train_status_list_adapter_row_station);
        }
    }
}
