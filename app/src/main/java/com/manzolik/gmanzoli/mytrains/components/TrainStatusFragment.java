package com.manzolik.gmanzoli.mytrains.components;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.service.TrainStatusService;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Frammento che visualizza lo stato del treno
 */
public class TrainStatusFragment extends Fragment implements TrainStatusService.TrainStatusServiceListener {
    private static final String TRAIN_CODE = "param1";
    private static final String DEPT_STATION= "param2";

    private String trainCode;
    private Station deptStation;


    private TextView trainCodeTextView;
    private TextView trainDelayTextView;
    private TextView trainDepartureTextView;
    private TextView trainLastSeenTextView;
    private TextView trainArrivalTextView;
    private View cardView;

    private ProgressDialog dialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    public TrainStatusFragment() {
        // Required empty public constructor
    }


    public static TrainStatusFragment newInstance(String trainCode, Station deptStation) {
        TrainStatusFragment fragment = new TrainStatusFragment();
        Bundle args = new Bundle();
        args.putString(TRAIN_CODE, trainCode);
        args.putSerializable(DEPT_STATION, deptStation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trainCode = getArguments().getString(TRAIN_CODE);
            deptStation = (Station) getArguments().getSerializable(DEPT_STATION);

            TrainStatusService tss = new TrainStatusService();
            tss.getStatusForTrain(trainCode, deptStation.getCode(), this);
            dialog = new ProgressDialog(getContext());
            dialog.setMessage("Caricamento dei dati");
            dialog.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_train_status, container, false);
        cardView = view.findViewById(R.id.train_status_fragment_card_view);
        trainCodeTextView = (TextView) view.findViewById(R.id.train_status_fragment_train_code);
        trainDelayTextView = (TextView) view.findViewById(R.id.train_status_fragment_delay);
        trainDepartureTextView = (TextView) view.findViewById(R.id.train_status_fragment_departure_station);
        trainLastSeenTextView = (TextView) view.findViewById(R.id.train_status_fragment_last_update);
        trainArrivalTextView = (TextView) view.findViewById(R.id.train_status_fragment_station);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.train_status_fragment_refresh);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                TrainStatusService tss = new TrainStatusService();
                tss.getStatusForTrain(trainCode, deptStation.getCode(), TrainStatusFragment.this);
                dialog = new ProgressDialog(getContext());
                dialog.setMessage("Aggiornamento dei dati");
                dialog.show();
            }
        });


        cardView.setVisibility(View.GONE);
        return view;
    }


    @Override
    public void onTrainStatusSuccess(TrainStatus status) {
        cardView.setVisibility(View.VISIBLE);
        dialog.hide();
        if (!status.isSuppressed()) {
            SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.getDefault());

            trainCodeTextView.setText(status.getTrainDescription());

            trainDelayTextView.setText(String.format("Ritardo %d'", status.getDelay()));
            if (status.getDelay() > 0){
                trainDelayTextView.setTextColor(Color.RED);
            } else {
                trainDelayTextView.setTextColor(0x388E3C); //Verde scuro
            }

            String departureInfo = String.format("%s - %s",
                                    status.getDepartureStationName(),
                                    format.format(status.getExpectedDeparture().getTime()) );

            trainDepartureTextView.setText(departureInfo);
            String lastUpdate = String.format("%s - %s",
                                    status.getLastCheckedStation(),
                                    format.format(status.getLastUpdate().getTime()) );

            trainLastSeenTextView.setText(lastUpdate);

            String arrivalInfo = String.format("%s - %s",
                    status.getArrivalStationName(),
                    format.format(status.getExpectedArrival().getTime()) );
            trainArrivalTextView.setText(arrivalInfo);
        } else {
            // Il treno è stato soppresso
            trainDelayTextView.setText("SOPPRESSO");
            trainDelayTextView.setTextColor(Color.RED);
            trainDelayTextView.setVisibility(TextView.VISIBLE);
        }
    }

    @Override
    public void onTrainStatusFailure(Exception e) {
        dialog.hide();
        System.err.println(e.getMessage());
        Toast.makeText(getContext(), "Si è verificato un problema, non è stato possibile reperire" +
                "lo stato del treno", Toast.LENGTH_LONG).show();
    }
}
