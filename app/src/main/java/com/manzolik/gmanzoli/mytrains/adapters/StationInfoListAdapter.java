package com.manzolik.gmanzoli.mytrains.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.StationInfo;

import java.util.List;

public class StationInfoListAdapter
        extends RecyclerView.Adapter<StationInfoListAdapter.StationInfoItemHolder>{

    private static final String TAG = StationInfoListAdapter.class.getSimpleName();

    private List<StationInfo> mStationInfoList;
    private Context mContext;
    private OnStationInfoSelectedListener mListener;


    public StationInfoListAdapter(Context context, List<StationInfo> mStationInfoList) {
        this.mStationInfoList = mStationInfoList;
        this.mContext = context;
    }

    @Override
    public StationInfoItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_adapter_row_station_info, parent, false);
        return new StationInfoItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StationInfoItemHolder holder, int position) {
        final StationInfo stationInfo = mStationInfoList.get(position);

        ;

        holder.descText.setText(String.format("%s delle %s", stationInfo.getTrainDescription(), stationInfo.getTrainTime()));
        holder.infoText.setText(stationInfo.getTrainInfo());

        if (stationInfo.getTrainDelay().equals("0")) {
            holder.delayText.setText("In orario");
        } else {
            holder.delayText.setText(String.format(mContext.getString(R.string.delay_field), stationInfo.getTrainDelay()));
        }


        String trackText = stationInfo.getTrainRealTrack();
        if (trackText == null || trackText.equals("null")) {
            trackText = stationInfo.getTrainExpectedTrack();
        }
        holder.trackText.setText(String.format(mContext.getString(R.string.track_field), trackText));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onClick - " + stationInfo.toString());
            if (mListener != null) {
                mListener.onStationInfoSelected(stationInfo);
            }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStationInfoList.size();
    }

    public void setItems(List<StationInfo> infos){
        this.mStationInfoList = infos;
        this.notifyDataSetChanged();
    }

    static class StationInfoItemHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView descText;
        final TextView infoText;
        //final TextView timeText;
        final TextView delayText;
        final TextView trackText;

        StationInfoItemHolder(View itemView) {
            super(itemView);
            view = itemView;
            descText = (TextView) itemView.findViewById(R.id.station_info_list_adapter_desc);
            infoText = (TextView) itemView.findViewById(R.id.station_info_list_adapter_info);
            //timeText = (TextView) itemView.findViewById(R.id.station_info_list_adapter_time);
            delayText = (TextView) itemView.findViewById(R.id.station_info_list_adapter_delay);
            trackText = (TextView) itemView.findViewById(R.id.station_info_list_adapter_track);
        }
    }

    /*
    * Gestione del listener
    * */

    public void setOnStationInfoSelectedListener(OnStationInfoSelectedListener mListener) {
        this.mListener = mListener;
    }

    public void removeOnStationInfoSelectedListener(){
        this.mListener = null;
    }

    public interface OnStationInfoSelectedListener{
        void onStationInfoSelected(StationInfo info);
    }

}
