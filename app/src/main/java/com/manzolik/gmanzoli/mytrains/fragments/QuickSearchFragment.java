package com.manzolik.gmanzoli.mytrains.fragments;

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
import com.manzolik.gmanzoli.mytrains.TrainStatusActivity;
import com.manzolik.gmanzoli.mytrains.data.Train;

/*
* Fragment che si occupa di visualizzare la form di ricerca di un treno (FindTrainFragment).
* Implementa la gestione della selezione di un treno e rimanda a `TrainstatusActivity` la
* visualizzazione dello stato.
* */

public class QuickSearchFragment extends Fragment
        implements FindTrainFragment.OnTrainFoundListener {

    private static final String TAG = QuickSearchFragment.class.getSimpleName();

    public QuickSearchFragment() {
        // Required empty public constructor
    }


    public static QuickSearchFragment newInstance() {
        QuickSearchFragment fragment = new QuickSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /*
    * INZIO - GESTIONE LIFECYCLE
    * */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frame_container, container, false);


        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FindTrainFragment fragment = FindTrainFragment.newInstance();

            // Visualizza FindTrainFragment
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }


        return view;
    }

    /*
    * FINE - GESTIONE LIFECYCLE
    * */


    /*
    * onTrainFound: Metodo invocato quando l'utente ha scelto correttamente il treno, avvia
    * TrainStatusActivity per la visualizzazione delle informazioni del treno
    * */
    @Override
    public void onTrainFound(Train train) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Codice treno selezionato: " + train.getCode());

        Intent i = new Intent(getContext(), TrainStatusActivity.class);
        i.putExtra(TrainStatusActivity.INTENT_TRAIN, train);
        startActivity(i);
    }
}
