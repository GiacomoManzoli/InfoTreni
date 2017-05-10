package com.manzolik.gmanzoli.mytrains.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.utils.LocationUtils;

public class StationDescriptionFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_STATION = "param1";
    private static final String TAG = StationDescriptionFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private Station mStation;

    private MapView mMapView;

    public StationDescriptionFragment() {
        // Required empty public constructor
    }


    public static StationDescriptionFragment newInstance(Station station) {
        StationDescriptionFragment fragment = new StationDescriptionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATION, station);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");


        if (savedInstanceState != null) {
            mStation = (Station) savedInstanceState.getSerializable(ARG_STATION);
        } else if (getArguments() != null) {
            mStation = (Station) getArguments().getSerializable(ARG_STATION);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_description, container, false);

        TextView stationNameView = (TextView) view.findViewById(R.id.station_description_fragment_name);
        stationNameView.setText(mStation.getName());

        TextView stationAddressView = (TextView) view.findViewById(R.id.station_description_fragment_address);
        stationAddressView.setText(LocationUtils.getAddress(getContext(), mStation.getLatitude(), mStation.getLongitude()));

        mMapView = (MapView) view.findViewById(R.id.station_description_fragment_map);
        /*
        * Servono stando a:
        * http://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
        * */
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);


        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        outState.putSerializable(ARG_STATION, mStation);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        LatLng latLng = new LatLng(mStation.getLatitude(), mStation.getLongitude());

        MarkerOptions stationMarketOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .title(mStation.getName())
                .snippet(LocationUtils.getAddress(getContext(), mStation.getLatitude(), mStation.getLongitude()));

        mGoogleMap.addMarker(stationMarketOptions);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        //mGoogleMap.setMyLocationEnabled(true);

    }
}
