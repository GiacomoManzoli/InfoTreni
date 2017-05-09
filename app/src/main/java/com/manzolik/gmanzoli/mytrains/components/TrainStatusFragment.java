package com.manzolik.gmanzoli.mytrains.components;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.TrainStop;
import com.manzolik.gmanzoli.mytrains.drawer.TrainStopListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Frammento che visualizza lo stato del treno
 */
public class TrainStatusFragment extends Fragment {

    private static final String TAG = TrainStatusFragment.class.getSimpleName();

    private static final String PARAM_TRAIN_STATUS = "param1";

    private TrainStatus mStatus;

    private TextView mTrainCodeTextView;
    private TextView mTrainDelayTextView;
    private TextView mTrainLastSeenTextView;
    private RecyclerView mRecyclerView;


    public TrainStatusFragment() {
        // Required empty public constructor
    }


    public static TrainStatusFragment newInstance() {
        TrainStatusFragment fragment = new TrainStatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static TrainStatusFragment newInstance(TrainStatus status) {
        TrainStatusFragment fragment = new TrainStatusFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_TRAIN_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mStatus = (TrainStatus) savedInstanceState.getSerializable(PARAM_TRAIN_STATUS);
        } else if (getArguments() != null) {
            mStatus = (TrainStatus) getArguments().getSerializable(PARAM_TRAIN_STATUS);
        }

        if (BuildConfig.DEBUG) {
            if (mStatus != null) {
                Log.d(TAG, "Status: " + mStatus.toString());
            } else {
                Log.d(TAG, "Status: null");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_train_status, container, false);
        mTrainCodeTextView = (TextView) view.findViewById(R.id.train_status_fragment_train_code);
        mTrainDelayTextView = (TextView) view.findViewById(R.id.train_status_fragment_delay);
        mTrainLastSeenTextView = (TextView) view.findViewById(R.id.train_status_fragment_last_update);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.train_status_fragment_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        TrainStopListAdapter adapter = new TrainStopListAdapter(getContext(), new ArrayList<TrainStop>());
        mRecyclerView.setAdapter(adapter);

        if (mStatus != null) {
            updateStatus(mStatus);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(PARAM_TRAIN_STATUS, mStatus);
    }


    public void updateStatus(TrainStatus status) {
        if (BuildConfig.DEBUG) Log.d(TAG, "updateStatus");

        mStatus = status;
        switch (status.getTrainStatusInfo()) {
            case STATUS_REGULAR:
                SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());

                mTrainCodeTextView.setText(status.getTrainDescription());

                mTrainDelayTextView.setText(String.format(Locale.getDefault(), "Ritardo %d'", status.getDelay()));
                if (status.getDelay() > 0){
                    mTrainDelayTextView.setTextColor(Color.RED);
                } else {
                    mTrainDelayTextView.setTextColor(0x388E3C); //Verde scuro
                }

                if (status.isDeparted() && status.getLastUpdate() != null){
                    String lastUpdate = String.format("%s - %s",
                            status.getLastCheckedStation(),
                            format.format(status.getLastUpdate().getTime()) );
                    mTrainLastSeenTextView.setText(lastUpdate);
                } else {
                    mTrainLastSeenTextView.setText("Treno non ancora rilevato");
                }

                TrainStopListAdapter adapter = (TrainStopListAdapter) mRecyclerView.getAdapter();
                adapter.setItems(status.getStops());

                break;
            case STATUS_SUPPRESSED:
                // Il treno è stato soppresso
                mTrainDelayTextView.setText("SOPPRESSO");
                mTrainDelayTextView.setTextColor(Color.RED);
                mTrainDelayTextView.setVisibility(TextView.VISIBLE);
                break;
            default:
                mTrainDelayTextView.setText("Altro");
                mTrainDelayTextView.setTextColor(Color.YELLOW);
                mTrainDelayTextView.setVisibility(TextView.VISIBLE);
        }
    }

}
