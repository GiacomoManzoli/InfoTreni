package com.manzolik.gmanzoli.mytrains.components;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.TrainStatusActivity;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.StationInfo;
import com.manzolik.gmanzoli.mytrains.data.Train;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.data.db.TrainDAO;
import com.manzolik.gmanzoli.mytrains.drawer.StationInfoListAdapter;
import com.manzolik.gmanzoli.mytrains.service.StationStatusService;

import java.util.ArrayList;
import java.util.List;


public class StationStatusInfosFragment
        extends Fragment
        implements StationStatusService.StationStatusListener, StationInfoListAdapter.OnStationInfoSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_STATION = "param1";
    private static final String ARG_QUERY_TYPE = "query_type";

    private static final String TAG = StationStatusInfosFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mNoElementText;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ProgressDialog mDialog;


    private Station mStation;
    private StationStatusService.StatusInfoQueryType mQueryType;

    public StationStatusInfosFragment() {
        // Required empty public constructor
    }


    public static StationStatusInfosFragment newInstanceArrivals(Station s) {
        return  newInstance(s, StationStatusService.StatusInfoQueryType.QUERY_ARRIVAL);
    }

    public static StationStatusInfosFragment newInstanceDepartures(Station s) {
        return  newInstance(s, StationStatusService.StatusInfoQueryType.QUERY_DEPARTURE);
    }


    public static StationStatusInfosFragment newInstance(Station station, StationStatusService.StatusInfoQueryType queryType) {
        StationStatusInfosFragment fragment = new StationStatusInfosFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATION, station);
        args.putSerializable(ARG_QUERY_TYPE, queryType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        if (savedInstanceState != null ) {
            mStation = (Station) savedInstanceState.getSerializable(ARG_STATION);
            mQueryType= (StationStatusService.StatusInfoQueryType) savedInstanceState.getSerializable(ARG_QUERY_TYPE);
        } else if (getArguments() != null) {
            mStation = (Station) getArguments().getSerializable(ARG_STATION);
            mQueryType= (StationStatusService.StatusInfoQueryType) getArguments().getSerializable(ARG_QUERY_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mNoElementText = (TextView) view.findViewById(R.id.recycler_view_fragment_empty_text);

        switch (mQueryType) {
            case QUERY_DEPARTURE:
                mNoElementText.setText(R.string.no_train_leaving);
                break;
            case QUERY_ARRIVAL:
                mNoElementText.setText(R.string.no_train_arriving);
                break;
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.recycler_view_fragment_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_fragment_recycler);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(new StationInfoListAdapter(getContext(), new ArrayList<StationInfo>()));

        mNoElementText.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        StationStatusService stationStatusService = new StationStatusService();
        stationStatusService.getStationInfos(mStation, mQueryType, this);

        mDialog = new ProgressDialog(getContext());
        mDialog.setMessage("Caricamento dei dati in corso");
        mDialog.show();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        StationInfoListAdapter adapter = (StationInfoListAdapter) mRecyclerView.getAdapter();
        adapter.setOnStationInfoSelectedListener(this);
        if (adapter.getItemCount() > 0) {
            mNoElementText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mNoElementText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(ARG_STATION, mStation);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        StationInfoListAdapter adapter = (StationInfoListAdapter) mRecyclerView.getAdapter();
        adapter.removeOnStationInfoSelectedListener();
    }

    @Override
    public void onStationStatusResult(List<StationInfo> infos, StationStatusService.StatusInfoQueryType queryType) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        if (mQueryType == queryType) {
            StationInfoListAdapter adapter = (StationInfoListAdapter) mRecyclerView.getAdapter();
            adapter.setItems(infos);
            if (adapter.getItemCount() > 0) {
                mNoElementText.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mNoElementText.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        } else {
            if (BuildConfig.DEBUG) Log.e(TAG, "Errore! queryType non coincide");
        }

    }

    @Override
    public void onStationStatusFailure(Exception exc) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (BuildConfig.DEBUG) Log.e(TAG, exc.getMessage());
    }

    @Override
    public void onStationInfoSelected(StationInfo info) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStationInfoSelected " + info.toString());
        Intent i = new Intent(getContext(), TrainStatusActivity.class);
        String trainCode = info.getTrainCode();
        String trainDepartureCode = info.getTrainDepartureCode();
        Station departureStation = (new StationDAO(getContext())).getStationFromCode(trainDepartureCode);
        Train t = new Train(-1, trainCode, departureStation);
        i.putExtra(TrainStatusActivity.INTENT_TRAIN, t);
        startActivity(i);
    }

    @Override
    public void onRefresh() {
        StationStatusService stationStatusService = new StationStatusService();
        stationStatusService.getStationInfos(mStation, mQueryType, this);
    }
}
