package com.manzolik.gmanzoli.mytrains.components;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.NoConnectivityActivity;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusService;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Frammento che visualizza lo stato del treno
 */
public class TrainStatusFragment extends Fragment
        implements TrainStatusService.TrainStatusServiceListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = TrainStatusFragment.class.getSimpleName();

    private static final String PARAM_TRAIN = "param1";

    private Train mTrain;


    private TextView mTrainCodeTextView;
    private TextView mTrainDelayTextView;
    private TextView mTrainDepartureTextView;
    private TextView mTrainLastSeenTextView;
    private TextView mTrainArrivalTextView;
    private View mCardView;

    private ProgressDialog mDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public TrainStatusFragment() {
        // Required empty public constructor
    }

    public static TrainStatusFragment newInstance(Train train) {
        TrainStatusFragment fragment = new TrainStatusFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_TRAIN, train);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrain = (Train) getArguments().getSerializable(PARAM_TRAIN);
            if (BuildConfig.DEBUG) Log.d(TAG, "Train: " + mTrain.toString());
            // Caricamento dei dati spostato in onResume
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_train_status, container, false);
        mCardView = view.findViewById(R.id.train_status_fragment_card_view);
        mTrainCodeTextView = (TextView) view.findViewById(R.id.train_status_fragment_train_code);
        mTrainDelayTextView = (TextView) view.findViewById(R.id.train_status_fragment_delay);
        mTrainDepartureTextView = (TextView) view.findViewById(R.id.train_status_fragment_departure_station);
        mTrainLastSeenTextView = (TextView) view.findViewById(R.id.train_status_fragment_last_update);
        mTrainArrivalTextView = (TextView) view.findViewById(R.id.train_status_fragment_station);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.train_status_fragment_refresh);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mCardView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        TrainStatusService tss = new TrainStatusService();
        tss.getStatusForTrain(mTrain, this);
        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("Caricamento dei dati");
        mDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null){
            mDialog.dismiss();
        }
    }


    /*
    * SwipeLayout - onRefresh
    * Aggiorna i dati
    * */
    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        TrainStatusService tss = new TrainStatusService();
        tss.getStatusForTrain(mTrain, this);
        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("Aggiornamento dei dati");
        mDialog.show();
    }

    @Override
    public void onTrainStatusSuccess(TrainStatus status) {
        mCardView.setVisibility(View.VISIBLE);
        mDialog.dismiss();
        if (!status.isSuppressed()) {
            SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());

            mTrainCodeTextView.setText(status.getTrainDescription());

            mTrainDelayTextView.setText(String.format(Locale.getDefault(), "Ritardo %d'", status.getDelay()));
            if (status.getDelay() > 0){
                mTrainDelayTextView.setTextColor(Color.RED);
            } else {
                mTrainDelayTextView.setTextColor(0x388E3C); //Verde scuro
            }

            String departureInfo = String.format("%s - %s",
                                    status.getDepartureStationName(),
                                    format.format(status.getExpectedDeparture().getTime()) );

            mTrainDepartureTextView.setText(departureInfo);
            String lastUpdate = String.format("%s - %s",
                                    status.getLastCheckedStation(),
                                    format.format(status.getLastUpdate().getTime()) );

            mTrainLastSeenTextView.setText(lastUpdate);

            String arrivalInfo = String.format("%s - %s",
                    status.getArrivalStationName(),
                    format.format(status.getExpectedArrival().getTime()) );
            mTrainArrivalTextView.setText(arrivalInfo);
        } else {
            // Il treno è stato soppresso
            mTrainDelayTextView.setText("SOPPRESSO");
            mTrainDelayTextView.setTextColor(Color.RED);
            mTrainDelayTextView.setVisibility(TextView.VISIBLE);
        }
    }

    @Override
    public void onTrainStatusFailure(Exception exc) {
        mDialog.dismiss();
        try {
            throw exc;
        } catch (UnknownHostException e) {
            // No internet connection
            Intent i = new Intent(getContext(), NoConnectivityActivity.class);
            startActivity(i);
            getActivity().finish();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage());
        }
    }


}
