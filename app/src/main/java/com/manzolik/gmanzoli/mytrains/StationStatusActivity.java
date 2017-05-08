package com.manzolik.gmanzoli.mytrains;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.manzolik.gmanzoli.mytrains.data.Station;

public class StationStatusActivity extends AppCompatActivity {
    private static final String TAG = StationStatusActivity.class.getSimpleName();

    public static final String INTENT_STATION = "intent_station";

    private Station mStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_status);

        mStation = (Station) getIntent().getSerializableExtra(INTENT_STATION);

        // Aggiorna il titolo dell'ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mStation.toString());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
}
