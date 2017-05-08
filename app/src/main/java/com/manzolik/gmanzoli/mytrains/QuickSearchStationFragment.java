package com.manzolik.gmanzoli.mytrains;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.manzolik.gmanzoli.mytrains.components.FindStationFragment;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.utils.LocationUtils;


public class QuickSearchStationFragment extends Fragment
        implements FindStationFragment.OnStationSelectedListener,
        StationDAO.OnFindNearestStationAsyncListener, View.OnClickListener {

    private static final String TAG = QuickSearchStationFragment.class.getSimpleName();

    private Button mGeohintButton;
    private ProgressBar mGeohintProgress;
    private Station mNearestStation;

    public QuickSearchStationFragment() {
        // Required empty public constructor
    }


    public static QuickSearchStationFragment newInstance() {
        QuickSearchStationFragment fragment = new QuickSearchStationFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quick_search_station, container, false);

        View geohintView = view.findViewById(R.id.quick_search_station_fragment_geohint_view);
        mGeohintButton = (Button) view.findViewById(R.id.quick_search_station_fragment_geohint_button);
        mGeohintButton.setOnClickListener(this);
        mGeohintProgress = (ProgressBar) view.findViewById(R.id.quick_search_station_fragment_geohint_progress);


        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView - nessuno stato, creo il fragment");
            // Costruisco il fragment solo se non è un restore
            FragmentManager fragmentManager = getChildFragmentManager();
            FindStationFragment fragment = FindStationFragment.newInstance();

            // Visualizza FindStationFragment
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.quick_search_station_fragment_main_frame, fragment);
            ft.commit();
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean geofilteringEnabled = sharedPref.getBoolean(SettingsFragment.NOTIFICATION_LOCATION_FILTERING, false);

        if (geofilteringEnabled){
            Location lastLocation = LocationUtils.getLastLocation(getContext());
            if (lastLocation != null) {
                mGeohintButton.setVisibility(View.GONE);
                mGeohintProgress.setVisibility(View.VISIBLE);
                StationDAO stationDAO = new StationDAO(getContext());
                stationDAO.findNearestStationAsync(lastLocation, this);
            } else {
                geohintView.setVisibility(View.GONE);
            }
        } else {
            geohintView.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
        * Callback per la ricerca asincrona della stazione più vicina
        * */
    @Override
    public void onFindNearestStation(Station station) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Stazione più vicina: " + station.toString());
        mGeohintProgress.setVisibility(View.GONE);
        mGeohintButton.setVisibility(View.VISIBLE);
        mGeohintButton.setText(station.getName());
        mNearestStation = station;
    }

    /*
    * View.OnClick
    * */
    @Override
    public void onClick(View v) {
        if (v.getId() == mGeohintButton.getId()) {
            selectStation(mNearestStation);
        }
    }

    /*
    * Callback per la selezione di una stazione da FindStationFragment
    * */
    @Override
    public void onStationSelected(Station station) {
        selectStation(station);
    }

    /*
    * Passa a StationStatusActivity
    * */
    private void selectStation(Station station) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Stazione selezionata: " + station.toString());
        Intent i = new Intent(getContext(), StationStatusActivity.class);
        i.putExtra(StationStatusActivity.INTENT_STATION, station);
        startActivity(i);
    }



}
