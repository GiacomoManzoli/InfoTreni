package com.manzolik.gmanzoli.mytrains.fragments.main;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manzolik.gmanzoli.mytrains.BuildConfig;
import com.manzolik.gmanzoli.mytrains.R;
import com.manzolik.gmanzoli.mytrains.StationStatusActivity;
import com.manzolik.gmanzoli.mytrains.data.Station;
import com.manzolik.gmanzoli.mytrains.fragments.FindStationFragment;


public class QuickSearchStationFragment extends Fragment
        implements FindStationFragment.OnStationSelectedListener {

    private static final String TAG = QuickSearchStationFragment.class.getSimpleName();

    private ProgressDialog mProgress;

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
        View view = inflater.inflate(R.layout.fragment_frame_container, container, false);

        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView - nessuno stato, creo il fragment");
            // Costruisco il fragment solo se non è un restore
            FragmentManager fragmentManager = getChildFragmentManager();
            FindStationFragment fragment = FindStationFragment.newInstance();

            // Visualizza FindStationFragment
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }


        return view;
    }


    /*
    * Callback per la selezione di una stazione da FindStationFragment
    * */
    @Override
    public void onStationSelected(Station station) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Stazione selezionata: " + station.toString());

        /*
        * Se non puoi sconfiggerli, unisciti a loro.
        * Questi:
        *   I/Google Maps Android API: Google Play services package version: 10298480
        * ci mettono circa 1 secondo a caricarsi, creando un fastidioso lag tra la pressione del
        * pulsante e la comparsa di StationStatusActivity (che visualizza una mappa).
        * Non c'è un modo di pre-caricarli in background, anche quello proposto qua:
        * http://stackoverflow.com/questions/26178212/first-launch-of-activity-with-google-maps-is-very-slow
        * non funziona.
        * */

        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Caricamento in corso...");
        mProgress.show();

        Intent i = new Intent(getContext(), StationStatusActivity.class);
        i.putExtra(StationStatusActivity.INTENT_STATION, station);
        startActivity(i);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG) Log.d(TAG, "onStop");
        if (mProgress != null) mProgress.dismiss();
    }
}
