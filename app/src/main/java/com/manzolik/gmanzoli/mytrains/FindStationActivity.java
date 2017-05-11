package com.manzolik.gmanzoli.mytrains;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.manzolik.gmanzoli.mytrains.fragments.FindStationFragment;
import com.manzolik.gmanzoli.mytrains.data.Station;

public class FindStationActivity extends AppCompatActivity
        implements FindStationFragment.OnStationSelectedListener {

    private static final String TAG = FindStationActivity.class.getSimpleName();

    public static final String INTENT_TITLE = "activity_title";
    public static final String SELECTED_STATION = "result_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_fragment);

        //String fragmentTitle = getString("Seleziona stazione");
        String fragmentTitle = "Seleziona stazione";

        if (getIntent().getStringExtra(INTENT_TITLE) != null) {
            fragmentTitle = getIntent().getStringExtra(INTENT_TITLE);
        }

        // Aggiorna il titolo dell'ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(fragmentTitle);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Creo il fragment solo se non c'è una savedInstance, ovvero solo al primo onCreate
        // Così facendo al cambio di layout, rimane visualizzato il fragment che precedentemente
        // era visualizzato
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FindStationFragment fragment = FindStationFragment.newInstance();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }
    }

    /* Gestione dei pulsanti nella toolbar*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            // Pulsante indietro
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                if (BuildConfig.DEBUG) Log.i(TAG, "C'è un Fragment, pop del backstack");
                fm.popBackStack();

            } else {
                if (BuildConfig.DEBUG) Log.i(TAG, "Ultimo fragment, comportamento di default");
                return super.onOptionsItemSelected(item);
            }
        }

        /* con return false la propagazione dell'evento continua e viene invocato onOptions...
        * del fragment contenuto*/
        return false;
    }

    /*
    * Metodo che viene invocato quando l'utente seleziona una stazione in FindStationFragment
    * */
    @Override
    public void onStationSelected(Station station) {
        Intent i = new Intent();
        i.putExtra(SELECTED_STATION, station);
        setResult(RESULT_OK, i);
        finish();
    }
}
