package com.manzolik.gmanzoli.mytrains;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.manzolik.gmanzoli.mytrains.components.TrainStatusFragment;
import com.manzolik.gmanzoli.mytrains.data.Train;

public class TrainStatusActivity extends AppCompatActivity {

    private static final String TAG = TrainStatusActivity.class.getSimpleName();

    public static final String INTENT_TRAIN = "train";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_status);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Andamento treno");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Train train = (Train) getIntent().getSerializableExtra(INTENT_TRAIN);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Codice treno: " + train.getCode());
            Log.d(TAG, "Codice partenza: " + train.getDepartureStation().getCode());
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        TrainStatusFragment trainStatusFragment = TrainStatusFragment.newInstance(train);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.train_status_activity_frame, trainStatusFragment);
        ft.commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
