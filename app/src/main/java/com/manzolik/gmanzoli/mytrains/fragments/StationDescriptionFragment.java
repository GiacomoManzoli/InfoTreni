package com.manzolik.gmanzoli.mytrains.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;
import com.manzolik.gmanzoli.mytrains.utils.LocationUtils;

public class StationDescriptionFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private static final String ARG_STATION = "param1";
    private static final String TAG = StationDescriptionFragment.class.getSimpleName();

    private Station mStation;

    ImageButton mFavoriteButton;
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
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_station_description, container, false);

        TextView stationNameView = (TextView) view.findViewById(R.id.station_description_fragment_name);
        stationNameView.setText(mStation.getName());

        TextView stationAddressView = (TextView) view.findViewById(R.id.station_description_fragment_address);
        stationAddressView.setText(LocationUtils.getAddress(getContext(), mStation.getLatitude(), mStation.getLongitude()));

        mFavoriteButton = (ImageButton) view.findViewById(R.id.favorite_button);
        mFavoriteButton.setOnClickListener(this);
        if (mStation.isFavorite()) {
            mFavoriteButton.setImageResource(R.mipmap.ic_star_accent_24dp);
        } else {
            mFavoriteButton.setImageResource(R.mipmap.ic_empty_star_accent_24dp);
        }

        mMapView = (MapView) view.findViewById(R.id.station_description_fragment_map);
        /*
        * http://stackoverflow.com/questions/19353255/how-to-put-google-maps-v2-on-a-fragment-using-viewpager
        * NOTA: la lag nella comparsa è probabilmente causata dal caricamento della mappa
        * */
        /* ANCHE questo trick non funziona
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                FragmentManager fm = getChildFragmentManager();
                SupportMapFragment mapFragment = SupportMapFragment
                        .newInstance();
                fm.beginTransaction()
                        .replace(R.id.map_fragment, mapFragment).commit();
                mapFragment.getMapAsync(StationDescriptionFragment.this);
            }
        }, 1000);*/

        mMapView.onCreate(savedInstanceState);
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
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng latLng = new LatLng(mStation.getLatitude(), mStation.getLongitude());

        MarkerOptions stationMarketOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .title(mStation.getName())
                .snippet(LocationUtils.getAddress(getContext(), mStation.getLatitude(), mStation.getLongitude()));

        map.addMarker(stationMarketOptions);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        //mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.favorite_button) {
            if (mStation != null) {
                StationDAO stationDAO = new StationDAO(getContext());
                mStation = stationDAO.updateFavoriteState(mStation, !mStation.isFavorite());
                if (mStation.isFavorite()) {
                    mFavoriteButton.setImageResource(R.mipmap.ic_star_accent_24dp);
                } else {
                    mFavoriteButton.setImageResource(R.mipmap.ic_empty_star_accent_24dp);
                }
            }
        }
    }
}
