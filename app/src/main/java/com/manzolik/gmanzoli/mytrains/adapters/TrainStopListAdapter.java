package com.manzolik.gmanzoli.mytrains.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.TrainStop;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class TrainStopListAdapter extends RecyclerView.Adapter<TrainStopListAdapter.TrainStopViewHolder> {

    private List<TrainStop> mList;
    private Context mContext;

    public TrainStopListAdapter(Context context, List<TrainStop> mList) {
        this.mList = mList;
        this.mContext = context;
    }

    @Override
    public TrainStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.list_adapter_row_train_stop, parent, false);
        return new TrainStopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrainStopViewHolder holder, int position) {
        TrainStop trainStop = mList.get(position);

        if (trainStop.getStatus() == TrainStop.TrainStopStatus.REGULAR
                || trainStop.getStatus() == TrainStop.TrainStopStatus.UNKNOWN) {
            holder.extraText.setVisibility(View.GONE);
        } else {
            holder.extraText.setText(R.string.train_stop_not_scheduled);
        }
        holder.stationNameText.setText(trainStop.getStationName());
        String departureTrack = trainStop.getDepartureTrack();
        if (departureTrack == null){
            departureTrack = trainStop.getDepartureTrackExpected();
            if (departureTrack == null) {
                departureTrack ="";
                holder.trackText.setVisibility(View.GONE);
            }
            holder.trackText.setVisibility(View.VISIBLE);
        }
        holder.trackText.setText(String.format(mContext.getString(R.string.train_stop_track), departureTrack));

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.arrivalText.setText(String.format(mContext.getString(R.string.train_stop_arrival), dateFormat.format(trainStop.getArrivalExpected())));

        holder.arrivalDelayText.setText(String.format(mContext.getString(R.string.train_stop_delay), delayString(trainStop.getArrivalDelay())));
        String arrivalStatus = "";
        if (trainStop.trainArrived()) {
            arrivalStatus = "Arrivato";
        }
        holder.arrivalStatusText.setText(arrivalStatus);
        holder.departureText.setText(String.format(mContext.getString(R.string.train_stop_departure), dateFormat.format(trainStop.getDepartureExpected())));

        holder.departureDelayText.setText(String.format(mContext.getString(R.string.train_stop_delay), delayString(trainStop.getDepartureDelay())));
        String departureStatus = "";
        if (trainStop.trainArrived()) {
            departureStatus = "Partito";
        }
        holder.departureStatusText.setText(departureStatus);
    }

    private String delayString(int delay) {
        String sing = "";
        sing = (delay > 0)? "+":sing;
        sing = (delay < 0)? "-":sing;
        return sing+String.valueOf(Math.abs(delay));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setItems(List<TrainStop> items) {
        this.mList = items;
        this.notifyDataSetChanged();
    }

    class TrainStopViewHolder extends RecyclerView.ViewHolder {
        TextView extraText;
        TextView stationNameText;
        TextView trackText;
        TextView arrivalText;
        TextView arrivalDelayText;
        TextView arrivalStatusText;
        TextView departureText;
        TextView departureDelayText;
        TextView departureStatusText;

        TrainStopViewHolder(View itemView) {
            super(itemView);
            extraText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_extra);
            stationNameText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_station_name);
            trackText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_track);
            arrivalText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_arrival);
            arrivalDelayText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_arrival_delay);
            arrivalStatusText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_arrival_status);
            departureText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_departure);
            departureDelayText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_departure_delay);
            departureStatusText = (TextView) itemView.findViewById(R.id.list_adapter_row_train_stop_departure_status);

        }
    }
}