package com.manzolik.gmanzoli.mytrains.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.TravelSolution;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TravelSolutionListAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SOLUTION = 0;
    private static final int TYPE_ELEMENT = 1;

    private List<Object> mSourceList;

    private OnTrainSelectListener mListener;

    public TravelSolutionListAdapter(List<TravelSolution> list) {
        mSourceList = new ArrayList<>();
        for (TravelSolution ts:list) {
            mSourceList.add(ts);
            for (TravelSolution.SolutionElement tse : ts.getElements()) {
                mSourceList.add(tse);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object val = mSourceList.get(position);

        if (val instanceof TravelSolution) {
            return TYPE_SOLUTION;
        } else if (val instanceof TravelSolution.SolutionElement) {
            return TYPE_ELEMENT;
        } else {
            return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SOLUTION:
                View itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_adapter_row_travel_solution_header, parent, false);
                return new TravelSolutionViewHolder(itemView);
            case TYPE_ELEMENT:
                View itemView2 = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_adapter_row_travel_solution_body, parent, false);
                return new TravelSolutionElementViewHolder(itemView2);
        }
        return null;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_SOLUTION:
                TravelSolutionViewHolder headerHolder = (TravelSolutionViewHolder) holder;
                TravelSolution solution = (TravelSolution) mSourceList.get(position);
                headerHolder.text.setText("da "+solution.getDepartureStationName()
                        + " a "+solution.getArrivalStaionName()
                        + " durata "+solution.getDuration());
                break;
            case TYPE_ELEMENT:
                TravelSolutionElementViewHolder elementHolder = (TravelSolutionElementViewHolder) holder;
                final TravelSolution.SolutionElement element = (TravelSolution.SolutionElement) mSourceList.get(position);

                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                elementHolder.codeText.setText(element.getCategory() + " " + element.getTrainCode());
                elementHolder.fromText.setText("da "+ element.getDeparture()
                        + " " + dateFormat.format(element.getDepartureTime().getTime()));
                elementHolder.toText.setText("per "+element.getArrival()
                        + " " + dateFormat.format(element.getArrivalTime().getTime()));

                elementHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TravelSolutionListAdapter.this.mListener != null) {
                            TravelSolutionListAdapter.this.mListener.onTrainSelected(element.getTrainCode());
                        }
                    }
                });
                break;
        }



    }

    @Override
    public int getItemCount() {
        return mSourceList.size();
    }


    public void setOnTrainSelectListener(OnTrainSelectListener mListener) {
        this.mListener = mListener;
    }

    public void removeOnTrainSelectListener() {
        this.mListener = null;
    }

    public interface OnTrainSelectListener {
        void onTrainSelected(String trainCode);
    }

    private class TravelSolutionViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        TravelSolutionViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.header_text);
        }

    }
    private class TravelSolutionElementViewHolder extends RecyclerView.ViewHolder {
        TextView codeText;
        TextView fromText;
        TextView toText;
        View view;

        TravelSolutionElementViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            codeText = (TextView) itemView.findViewById(R.id.train_code);
            fromText = (TextView) itemView.findViewById(R.id.train_departure);
            toText = (TextView) itemView.findViewById(R.id.train_arrival);

        }
    }
}



