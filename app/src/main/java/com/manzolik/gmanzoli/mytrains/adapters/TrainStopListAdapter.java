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

        String stationName = trainStop.getStationName();
        if (stationName.length() > 25) {
            stationName = stationName.substring(0, 22);
            stationName = stationName + "...";
        }
        holder.stationNameText.setText(stationName);

        // Binario di partenza
        String departureTrack = trainStop.getDepartureTrack();
        if (departureTrack == null){
            departureTrack = trainStop.getDepartureTrackExpected();
            if (departureTrack == null) {
                departureTrack = "--";
            }
        }
        holder.trackText.setText(String.format(mContext.getString(R.string.train_stop_track), departureTrack));

        // Informazioni sulla partenza
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (trainStop.getKind() != TrainStop.TrainStopKind.DEPARTURE) {
            // Se non è la stazione di partenza mostro l'orario di arrivo
            holder.arrivalText.setText(String.format(mContext.getString(R.string.train_stop_arrival), dateFormat.format(trainStop.getArrivalExpected())));

            holder.arrivalDelayText.setText(String.format(mContext.getString(R.string.train_stop_delay), delayString(trainStop.getArrivalDelay(), trainStop.trainArrived())));

            String arrivalStatus = "";
            if (trainStop.trainArrived()) {
                arrivalStatus = "Arrivato";
            }
            holder.arrivalStatusText.setText(arrivalStatus);
            // Servono perché se viene reciclata una view che li aveva nascosti
            // questi sono ancora nascosti
            holder.arrivalText.setVisibility(View.VISIBLE);
            holder.arrivalDelayText.setVisibility(View.VISIBLE);
            holder.arrivalStatusText.setVisibility(View.VISIBLE);
        } else {
            holder.arrivalText.setVisibility(View.GONE);
            holder.arrivalDelayText.setVisibility(View.GONE);
            holder.arrivalStatusText.setVisibility(View.GONE);
        }

        // Informazioni sull'arrivo
        if (trainStop.getKind() != TrainStop.TrainStopKind.ARRIVAL) {
            holder.departureText.setText(String.format(mContext.getString(R.string.train_stop_departure), dateFormat.format(trainStop.getDepartureExpected())));
            holder.departureDelayText.setText(String.format(mContext.getString(R.string.train_stop_delay), delayString(trainStop.getDepartureDelay(), trainStop.trainLeaved())));
            String departureStatus = "";
            if (trainStop.trainLeaved()) {
                departureStatus = "Partito";
            }
            holder.departureStatusText.setText(departureStatus);
            // Servono perché se viene reciclata una view che li aveva nascosti
            // questi sono ancora nascosti
            holder.departureDelayText.setVisibility(View.VISIBLE);
            holder.departureStatusText.setVisibility(View.VISIBLE);
            holder.departureText.setVisibility(View.VISIBLE);
        } else {
            holder.departureDelayText.setVisibility(View.GONE);
            holder.departureStatusText.setVisibility(View.GONE);
            holder.departureText.setVisibility(View.GONE);
        }


    }

    private String delayString(int delay, boolean known) {
        String sing = "";
        sing = (delay > 0)? "+":sing;
        sing = (delay < 0)? "-":sing;
        String num = (delay != 0 || known)? String.valueOf(Math.abs(delay)) : "-";
        return sing+num;
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
