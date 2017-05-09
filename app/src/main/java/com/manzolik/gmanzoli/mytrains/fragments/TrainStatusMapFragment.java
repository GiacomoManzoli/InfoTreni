package com.manzolik.gmanzoli.mytrains.fragments;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.data.TrainStatus;
import com.manzolik.gmanzoli.mytrains.data.TrainStop;
import com.manzolik.gmanzoli.mytrains.data.db.StationDAO;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TrainStatusMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = TrainStatusMapFragment.class.getSimpleName();
    private static final String PARAM_TRAIN_STATUS = "param1";

    private TrainStatus mStatus;
    private GoogleMap mGoogleMap;

    private MapView mMapView;

    public TrainStatusMapFragment() {
        // Required empty public constructor
    }

    public static TrainStatusMapFragment newInstance() {
        Bundle args = new Bundle();
        TrainStatusMapFragment fragment = new TrainStatusMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TrainStatusMapFragment newInstance(TrainStatus status) {
        TrainStatusMapFragment fragment = new TrainStatusMapFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_train_status_map, container, false);

        mMapView = (MapView) view.findViewById(R.id.map);
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
        if (mStatus != null) {
            updateStatus(mStatus);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        if (mStatus != null) {
            outState.putSerializable(PARAM_TRAIN_STATUS, mStatus);
        }
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

        setupMarker();
        //mGoogleMap.setMyLocationEnabled(true);

    }

    public void updateStatus(TrainStatus status) {
        mStatus = status;
        setupMarker();
    }

    private void setupMarker() {
        if (mStatus != null && mGoogleMap != null) {

            LatLng departureStationPosition = null;
            LatLng arrivalStationPosition = null;

            StationDAO stationDao = new StationDAO(getContext());

            TrainStop previousStop = null;
            LatLng previousLatLng = null;

            TrainStop lastStopChecked = null;
            TrainStop nextStop = null;
            boolean endOfTrack = false;

            for (TrainStop ts: mStatus.getStops()) {
                Station station = stationDao.getStationFromCode(ts.getStationCode());
                if (station == null) continue;

                LatLng latLng = new LatLng(station.getLatitude(), station.getLongitude());

                /*
                * Codice colore:
                * - Verde: stazione passata
                * - Orange: utlima stazione visualizzata
                * - Rosso: Stazione in programma
                * - Viola: stazione con problemi
                * */
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                Calendar arrival = Calendar.getInstance();
                arrival.setTime(ts.getArrivalExpected());
                arrival.setTimeInMillis( arrival.getTimeInMillis() + mStatus.getDelay()*60*1000);

                String message = "Arrivo previsto: " + dateFormat.format(arrival.getTime()); // considero anche il ritardo
                float markerHue = BitmapDescriptorFactory.HUE_RED;
                int segmentColorId = R.color.material_red;

                if (ts.trainLeaved()) {
                    markerHue = BitmapDescriptorFactory.HUE_GREEN;
                    segmentColorId = R.color.material_green;
                    int delay = ts.getDepartureDelay();
                    if (delay > 0) {
                        message = "Ritardo: " + ts.getDepartureDelay();
                    } else {
                        message = "In orario";
                    }
                } else if (previousStop != null
                        && previousStop.trainLeaved() // il treno ha lasciato la stazione precedente
                        && !ts.trainLeaved() // ma deve ancora lasciare questa
                        ){
                    // Questa è la prossima stazione visitata dal treno
                    segmentColorId = R.color.material_orange;
                    lastStopChecked = previousStop;
                    nextStop = ts;
                    markerHue = BitmapDescriptorFactory.HUE_ORANGE;

                } else if (previousStop != null
                        && previousStop.trainLeaved() // il treno ha lasciato la stazione precedente
                        && !ts.trainLeaved() // na non ha lasciato questa stazione
                        && ts.getKind() == TrainStop.TrainStopKind.ARRIVAL){
                    markerHue = BitmapDescriptorFactory.HUE_GREEN;
                    segmentColorId = R.color.material_green;
                    message = "Treno arrivato al capolinea";
                    endOfTrack = true;

                } else if (previousStop == null && ts.getKind() == TrainStop.TrainStopKind.DEPARTURE) {
                    markerHue = BitmapDescriptorFactory.HUE_ORANGE;
                    message = "Partenza prevista: " + dateFormat.format(ts.getDepartureExpected());
                } else if (ts.getKind() == TrainStop.TrainStopKind.SKIPPED){
                    markerHue = BitmapDescriptorFactory.HUE_VIOLET;
                    message = "Fermata non prevista";
                    segmentColorId = R.color.material_purple;
                }

                if (ts.getKind() == TrainStop.TrainStopKind.DEPARTURE) {
                    departureStationPosition = latLng;
                } else if (ts.getKind() == TrainStop.TrainStopKind.ARRIVAL) {
                    arrivalStationPosition = latLng;
                }


                MarkerOptions stationMarketOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                        .title(station.getName())
                        .snippet(message);;


                mGoogleMap.addMarker(stationMarketOptions);

                // mGoogleMap.addPolyline(null);
                if (previousLatLng != null) {
                    mGoogleMap.addPolyline(new PolylineOptions()
                    .add(previousLatLng, latLng)
                    .color(ContextCompat.getColor(getContext(), segmentColorId)));
                }

                previousLatLng = latLng;
                previousStop = ts;
            }

            // Marker del treno
            LatLng trainLatLng = null;
            String trainMessage = "Errore nella predizone del treno";
            if ( ! mStatus.isDeparted()) {
                // Il treno è ancora nella stazione di partenza
                Station depStation = stationDao.getStationFromCode(mStatus.getDepartureStationCode());
                if (depStation != null) {
                    trainLatLng = new LatLng(depStation.getLatitude(), depStation.getLongitude());
                    trainMessage = "Il treno deve ancora partire";
                }
            } else if (endOfTrack){
                // Il treno è arrivato a destinazione
                Station depStation = stationDao.getStationFromCode(mStatus.getArrivalStationCode());
                if (depStation != null) {
                    trainLatLng = new LatLng(depStation.getLatitude(), depStation.getLongitude());
                    trainMessage = "Il treno è arrivato a destinazione";
                }
            } else {
                if (lastStopChecked != null && nextStop != null) {
                    Calendar expectedArrival = Calendar.getInstance();
                    expectedArrival.setTime(nextStop.getArrivalExpected());
                    Calendar expectedDeparture = Calendar.getInstance();
                    expectedDeparture.setTime(lastStopChecked.getDepartureExpected());

                    long expectedDuration = expectedArrival.getTimeInMillis() - expectedDeparture.getTimeInMillis();
                    long departureTime = lastStopChecked.getDepartureExpected().getTime() + mStatus.getDelay()*60*1000;
                    long now = Calendar.getInstance().getTimeInMillis();

                    long timeProgress = now - departureTime;
                    double predictedProgress = (double) timeProgress /(double) expectedDuration;
                    if (BuildConfig.DEBUG) Log.d(TAG, "Progresso stimato "+String.valueOf(predictedProgress));

                    if (predictedProgress < 1 && predictedProgress >= 0){
                        Station s1 = stationDao.getStationFromCode(lastStopChecked.getStationCode());
                        Station s2 = stationDao.getStationFromCode(nextStop.getStationCode());
                        if (s1 != null && s2 != null) {
                            double x1 = s1.getLatitude();
                            double y1 = s1.getLongitude();
                            double x2 = s2.getLatitude();
                            double y2 = s2.getLongitude();

                            double x3 = (x2-x1)*predictedProgress + x1;
                            double y3 = (y2-y1)*predictedProgress + y1;

                            trainLatLng = new LatLng(x3,y3);
                            trainMessage = "Posizione stimata";
                        }
                    } else {
                        Station s = stationDao.getStationFromCode(nextStop.getStationCode());
                        if (s != null) {
                            trainLatLng = new LatLng(s.getLatitude(),s.getLongitude());
                            trainMessage = "Il treno dovrebbe essere arrivato nella stazione";
                        }
                    }


                }
            }

            if (trainLatLng != null) {
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(trainLatLng)
                        .title(mStatus.getTrainDescription())
                        .snippet(trainMessage)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_rock_n_roll_train_32dp)));
            }


            // Zoom sulla mappa
            if (arrivalStationPosition != null && departureStationPosition != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(arrivalStationPosition);
                builder.include(departureStationPosition);
                LatLngBounds bounds = builder.build();
                int padding = 200;
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }



        }
    }


}
