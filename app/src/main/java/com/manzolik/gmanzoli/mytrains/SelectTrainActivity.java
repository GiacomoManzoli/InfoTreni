package com.manzolik.gmanzoli.mytrains;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.manzolik.gmanzoli.mytrains.data.TravelSolution;
import com.manzolik.gmanzoli.mytrains.fragments.FindTrainFragment;
import com.manzolik.gmanzoli.mytrains.fragments.TravelSolutionListFragment;

import java.util.ArrayList;
import java.util.List;

public class SelectTrainActivity extends AppCompatActivity
        implements TravelSolutionListFragment.TrainSelectedListener {

    private static final String TAG = SelectTrainActivity.class.getSimpleName();

    public static final String INTENT_SOLUTIONS = "solutions";
    public static final String SELECTED_TRAIN = "train";


    private List<TravelSolution> mSolutions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_fragment);

        // Aggiorna il titolo dell'ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Treni trovati");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            //noinspection unchecked
            mSolutions = (List<TravelSolution>) savedInstanceState.getSerializable(INTENT_SOLUTIONS);
        } else {
            //noinspection unchecked
            mSolutions = (List<TravelSolution>) getIntent().getSerializableExtra(INTENT_SOLUTIONS);
        }

        // Creo il fragment solo se non c'è una savedInstance, ovvero solo al primo onCreate
        // Così facendo al cambio di layout, rimane visualizzato il fragment che precedentemente
        // era visualizzato
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            TravelSolutionListFragment fragment = TravelSolutionListFragment.newInstance((ArrayList<TravelSolution>) mSolutions);

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(INTENT_SOLUTIONS,(ArrayList<TravelSolution>) mSolutions);
    }

    /* Gestione dei pulsanti nella toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == android.R.id.home) {
            // Pulsante indietro
            onBackPressed();
        }
        return false;
    }

    @Override
    public void onTrainSelected(String trainCode) {
        Intent i = new Intent();
        i.putExtra(SELECTED_TRAIN, trainCode);
        setResult(RESULT_OK, i);
        finish();
    }
}
