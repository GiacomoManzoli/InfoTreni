package com.manzolik.gmanzoli.mytrains.components;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;


public class StationArrivalsFragment extends Fragment {
    private static final String ARG_STATION = "param1";
    private static final String TAG = StationArrivalsFragment.class.getSimpleName();

    private EditText mEditText;

    private Station mStation;

    public StationArrivalsFragment() {
        // Required empty public constructor
    }



    public static StationArrivalsFragment newInstance(Station station) {
        StationArrivalsFragment fragment = new StationArrivalsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATION, station);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

        if (getArguments() != null) {
            mStation = (Station) getArguments().getSerializable(ARG_STATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_station_arrivals, container, false);

        mEditText = (EditText) view.findViewById(R.id.edit_text);
        if (savedInstanceState != null) {
            mEditText.setText(savedInstanceState.getString("ASD"));
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(ARG_STATION, mStation);
    }


}
